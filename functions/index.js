const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * C:\Users\CanCONGER\AndroidStudioProjects\DeneCoz
 * firebase deploy --only functions
 */

// =========================================================================
// YENİ BİRLEŞTİRİLMİŞ FONKSİYON 1: DENEME OLUŞTURULDUĞUNDA (onCreate)
// =========================================================================
exports.onAttemptCreated = functions.firestore
    .document("attempts/{attemptId}")
    .onCreate(async (snapshot) => {
      const attemptData = snapshot.data();
      if (!attemptData) {
        console.log("Attempt data is missing.");
        return null;
      }

      const {
        studentId,
        examId,
        booklet,
        answers,
        alternativeChoice,
        examType, // Android tarafından bu alanın eklendiğini varsayıyoruz
      } = attemptData;

      // Gerekli veriler yoksa durdur
      if (!studentId || !examId || !booklet || !examType) {
        console.error("Attempt data is missing critical fields (studentId, examId, booklet, examType).");
        return null;
      }

      try {
        // --- 1. GEREKLİ TÜM VERİLERİ SADECE BİR KEZ ÇEK ---
        const examDocRef = db.collection("exams").doc(examId);
        const correctAnswersPromise = examDocRef
            .collection("booklets").doc(booklet)
            .collection("answerKey").get();
        const topicDistPromise = examDocRef
            .collection("booklets").doc(booklet)
            .collection("topicDistribution").get();
        const examDetailsPromise = examDocRef.get();
        const userDocRef = db.collection("students").doc(studentId);
        const userDocPromise = userDocRef.get();

        const [ 
          correctAnswersSnapshot,
          topicDistSnapshot,
          examDetailsSnapshot,
          userDoc,
        ] = await Promise.all([
          correctAnswersPromise,
          topicDistPromise,
          examDetailsPromise,
          userDocPromise,
        ]);

        if (!examDetailsSnapshot.exists || !userDoc.exists) {
          console.error(`Exam (${examId}) or User (${studentId}) not found.`);
          return null;
        }

        // --- 2. VERİLERİ HAZIRLA ---
        const correctAnswers = Object.fromEntries(
            correctAnswersSnapshot.docs.map((doc) => [doc.id, doc.data().correctAnswer])
        );
        const topicDistribution = Object.fromEntries(
            topicDistSnapshot.docs.map((doc) => [doc.id, doc.data()])
        );
        const examSubjects = examDetailsSnapshot.data().subjects || [];
        const subSubjects = examSubjects
            .map((s) => s.subSubjects)
            .find((ss) => ss !== undefined) || [];
        const userData = userDoc.data();

        // --- 3. ANALİZ MOTORUNU ÇALIŞTIR (HEM GENEL HEM KONU BAZINDA) ---
        let overallCorrect = 0;
        let overallIncorrect = 0;
        const topicPerformanceDelta = {}; // Konu bazında D/Y/B
        const leaderboardDocId = `${examId}_${studentId}`; // Lider tablosu ID'si

        for (const questionIndex in answers) {
          if (!Object.prototype.hasOwnProperty.call(answers, questionIndex)) continue;

          const studentAnswer = answers[questionIndex];
          const correctAnswer = correctAnswers[questionIndex];
          const topicInfo = topicDistribution[questionIndex];
          const uniqueTopicId = topicInfo?.topicId || `diger_${questionIndex}`;
          const topicNameForDisplay = topicInfo?.topicName || "Diğer";

          // Seçmeli ders kontrolü
          const subjectIdOfQuestion = uniqueTopicId.substring(0, uniqueTopicId.lastIndexOf("_"));
          const subjectInfo = subSubjects.find((ss) => ss.subjectId === subjectIdOfQuestion);
          if (subjectInfo?.isAlternative !== undefined && subjectInfo.subjectId !== alternativeChoice) {
            continue; // Bu soruyu analize katma
          }

          // Konu Performansı (topicPerformanceDelta) için D/Y/B say
          if (!topicPerformanceDelta[uniqueTopicId]) {
            topicPerformanceDelta[uniqueTopicId] = {
              name: topicNameForDisplay, correct: 0, incorrect: 0, empty: 0,
            };
          }

          // Genel Net (overall) ve Konu (delta) için D/Y/B say
          if (studentAnswer === "-" || !studentAnswer) {
            topicPerformanceDelta[uniqueTopicId].empty += 1;
          } else if (studentAnswer === correctAnswer) {
            overallCorrect++; // Genel doğruyu artır
            topicPerformanceDelta[uniqueTopicId].correct += 1;
          } else {
            overallIncorrect++; // Genel yanlışı artır
            topicPerformanceDelta[uniqueTopicId].incorrect += 1;
          }
        }
        const overallNet = overallCorrect - (overallIncorrect / 4.0);

        // --- 4. TEK BİR TRANSACTION İLE TÜM VERİTABANINI GÜNCELLE ---
        const batch = db.batch(); // Batch işlemi kullanmak, birden fazla yazma için daha iyidir.

        // GÖREV A: Kişisel En İyi Skoru (ProfileScreen) Güncelle
        const bestScores = userData.bestScores || {};
        const currentBestNet = bestScores[examType]?.net || -999;
        if (overallNet > currentBestNet) {
          const newBestScore = {
            net: overallNet, correct: overallCorrect, incorrect: overallIncorrect,
          };
          batch.update(userDocRef, {[`bestScores.${examType}`]: newBestScore});
        }

        // GÖREV B: Konu Performansını (Analiz Raporu) Güncelle
        for (const uniqueTopicId in topicPerformanceDelta) {
          if (Object.prototype.hasOwnProperty.call(topicPerformanceDelta, uniqueTopicId)) {
            const delta = topicPerformanceDelta[uniqueTopicId];
            const docId = `${studentId}_${uniqueTopicId}`;
            const performanceDocRef = db.collection("userTopicPerformance").doc(docId);
            batch.set(performanceDocRef, {
              studentId: studentId,
              topicId: uniqueTopicId,
              topicName: delta.name,
              totalCorrect: admin.firestore.FieldValue.increment(delta.correct),
              totalIncorrect: admin.firestore.FieldValue.increment(delta.incorrect),
              totalEmpty: admin.firestore.FieldValue.increment(delta.empty),
            }, {merge: true});
          }
        }

        // GÖREV C: Lider Tablolarını (LeaderboardScreen) Güncelle
        const leaderboardData = {
          attemptId: snapshot.id,
          examId: examId,
          studentId: studentId,
          studentName: userData.name || "Öğrenci Adı",
          city: userData.city || null,
          district: userData.district || null,
          school: userData.school || null,
          examType: examType,
          net: overallNet,
          correct: overallCorrect,
          incorrect: overallIncorrect,
          completedAt: admin.firestore.FieldValue.serverTimestamp(),
        };
        // Lider tablosu dökümanını (sadece en iyi skoru tutan) güncelle
        // Not: Burada 'set' kullanmak, o deneme için sadece en iyi skoru tutma mantığını uygular
        // (Eski kodu transaction'a çevirmek)
        const turkeyLbRef = db.collection("leaderboards_turkey").doc(leaderboardDocId);
        const provinceLbRef = db.collection("leaderboards_province").doc(leaderboardDocId);
        const districtLbRef = db.collection("leaderboards_district").doc(leaderboardDocId);

        const currentLeaderboardDoc = await turkeyLbRef.get(); // Transaction içinde get yapamayız, batch'e çevirdik
        const currentLeaderboardNet = currentLeaderboardDoc.data()?.net || -999;

        if (overallNet > currentLeaderboardNet) {
          batch.set(turkeyLbRef, leaderboardData, {merge: true});
          batch.set(provinceLbRef, leaderboardData, {merge: true});
          batch.set(districtLbRef, leaderboardData, {merge: true});
        }

        // GÖREV D: Gelişim Grafiği Verisini (DevelopmentScreen) Yaz
        const summaryDocRef = db.collection("attemptSummaries").doc(snapshot.id);
        const summaryData = {
          studentId: studentId,
          examId: examId,
          examType: examType,
          net: overallNet,
          correct: overallCorrect,
          incorrect: overallIncorrect,
          completedAt: admin.firestore.FieldValue.serverTimestamp(),
        };
        batch.set(summaryDocRef, summaryData);
        
        // Tüm işlemleri tek seferde onayla
        await batch.commit();

        console.log(`All analytics updated successfully for attempt ${snapshot.id}.`);
        return null;
      } catch (error) {
        console.error(`Error in onAttemptCreated for attempt ${snapshot.id}:`, error);
        return null;
      }
    });

// =========================================================================
// FONKSİYON 2: BİR DENEME KAYDI SİLİNDİĞİNDE EN YÜKSEK NETİ YENİDEN HESAPLA
// =========================================================================
exports.recalculateBestNetScoreOnDelete = functions.firestore
    .document("attempts/{attemptId}")
    .onDelete(async (snapshot) => {
      const deletedAttempt = snapshot.data();
      if (!deletedAttempt) return null;

      const {studentId, examId, examType, booklet, answers} = deletedAttempt;

      // Eğer deneme türü veya öğrenci ID'si yoksa, işlem yapamayız.
      if (!studentId || !examType) {
        console.error("Deleted attempt is missing studentId or examType.");
        return null;
      }

      try {
        const userDocRef = db.collection("students").doc(studentId);
        const userDoc = await userDocRef.get();
        if (!userDoc.exists) return null;

        const bestScores = userDoc.data().bestScores || {};
        const currentBestNet = bestScores[examType];

        // Eğer bu sınav türü için zaten bir rekor kayıtlı değilse, hiçbir şey yapma.
        if (!currentBestNet) {
          console.log(`No record found for ${examType}. Nothing to do.`);
          return null;
        }

        // --- 1. SİLİNEN DENEMENİN NETİNİ TAM OLARAK HESAPLA ---
        // Bu, yeniden hesaplamanın gerekli olup olmadığını anlamak için ZORUNLUDUR.
        const correctAnswersSnapshot = await db.collection("exams").doc(examId)
            .collection("booklets").doc(booklet)
            .collection("answerKey").get();

        const correctAnswers = Object.fromEntries(
            correctAnswersSnapshot.docs.map((doc) => [doc.id, doc.data().correctAnswer])
        );

        let deletedCorrect = 0;
        let deletedIncorrect = 0;
        for (const qIndex in answers) {
          if (Object.prototype.hasOwnProperty.call(answers, qIndex)) {
            if (answers[qIndex] === correctAnswers[qIndex]) deletedCorrect++;
            else if (answers[qIndex] !== "-") deletedIncorrect++;
          }
        }
        const deletedNet = deletedCorrect - (deletedIncorrect / 4.0);
        
        // --- 2. "AKILLI KONTROL": Gerekli Değilse Fonksiyondan Çık (Maliyet Tasarrufu) ---
        // Eğer silinen denemenin neti, mevcut rekordan daha düşükse,
        // rekoru etkilememiştir. Bu yüzden maliyetli işleme gerek yok.
        if (deletedNet < currentBestNet.net) {
          console.log(`Deleted net (${deletedNet}) is lower than record (${currentBestNet.net}). Recalculation not needed.`);
          return null;
        }

        // --- 3. "ACİL DURUM": Rekor deneme silindiği için yeniden hesaplama yap ---
        console.log(`Record attempt might have been deleted. Recalculating best net for ${examType}.`);

        const remainingAttemptsSnapshot = await db.collection("attempts")
            .where("studentId", "==", studentId)
            .where("examType", "==", examType)
            .get();

        let newBestScore = {net: -999, correct: 0, incorrect: 0};

        if (remainingAttemptsSnapshot.empty) {
          console.log(`No attempts left for ${examType}. Removing record.`);
          return userDocRef.update({[`bestScores.${examType}`]: admin.firestore.FieldValue.delete()});
        }

        // Kalan her deneme için neti yeniden hesapla (Bu, fonksiyonun en maliyetli kısmıdır)
        for (const attemptDoc of remainingAttemptsSnapshot.docs) {
          const attemptData = attemptDoc.data();
          const currentAnswers = attemptData.answers;
          // Her denemenin kendi doğru cevap anahtarını çek
          const caSnapshot = await db.collection("exams").doc(attemptData.examId)
              .collection("booklets").doc(attemptData.booklet)
              .collection("answerKey").get();
          
          const ca = Object.fromEntries(caSnapshot.docs.map((d) => [d.id, d.data().correctAnswer]));

          let correctCount = 0;
          let incorrectCount = 0;
          for (const qIndex in currentAnswers) {
            if (Object.prototype.hasOwnProperty.call(currentAnswers, qIndex)) {
              if (currentAnswers[qIndex] === ca[qIndex]) correctCount++;
              else if (currentAnswers[qIndex] !== "-") incorrectCount++;
            }
          }
          const currentNet = correctCount - (incorrectCount / 4.0);

          if (currentNet > newBestScore.net) {
            newBestScore = {net: currentNet, correct: correctCount, incorrect: incorrectCount};
          }
        }
        
        console.log(`Recalculation complete. New best net for ${examType} is ${newBestScore.net}`);
        return userDocRef.update({[`bestScores.${examType}`]: newBestScore});
      } catch (error) {
        console.error("Error in recalculateBestNetScoreOnDelete:", error);
        return null;
      }
    });