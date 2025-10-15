const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * C:\Users\CanCONGER\AndroidStudioProjects\DeneCoz
 * firebase deploy --only functions
 * -------
 * 'attempts' koleksiyonuna yeni bir kayıt eklendiğinde tetiklenir.
 */
exports.updateUserTopicPerformance = functions.firestore
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
      } = attemptData;

      try {
        const examDocRef = db.collection("exams").doc(examId);
        const correctAnswersPromise = examDocRef
            .collection("booklets").doc(booklet)
            .collection("answerKey").get();
        const topicDistPromise = examDocRef
            .collection("booklets").doc(booklet)
            .collection("topicDistribution").get();
        const examDetailsPromise = examDocRef.get();

        const [
          correctAnswersSnapshot,
          topicDistSnapshot,
          examDetailsSnapshot,
        ] = await Promise.all([
          correctAnswersPromise,
          topicDistPromise,
          examDetailsPromise,
        ]);

        const correctAnswers = Object.fromEntries(
            correctAnswersSnapshot.docs.map((doc) => [
              doc.id, doc.data().correctAnswer,
            ]));
        const topicDistribution = Object.fromEntries(
            topicDistSnapshot.docs.map((doc) => [
              doc.id, doc.data().topicId,
            ]));

        const examSubjects = examDetailsSnapshot.data().subjects || [];
        const subSubjects = examSubjects
            .map((s) => s.subSubjects)
            .find((ss) => ss !== undefined) || [];

        const topicPerformanceDelta = {};

        for (const questionIndex in topicDistribution) {
          if (Object.prototype.hasOwnProperty.call(
              topicDistribution, questionIndex)) {
            const topicName = topicDistribution[questionIndex];
            const studentAnswer = answers[questionIndex];
            const correctAnswer = correctAnswers[questionIndex];

            const subjectInfo = subSubjects.find((ss) => ss.name === topicName);
            if (
              subjectInfo?.isAlternative !== undefined &&
              subjectInfo.subjectId !== alternativeChoice
            ) {
              continue;
            }

            if (!topicPerformanceDelta[topicName]) {
              topicPerformanceDelta[topicName] = {
                correct: 0, incorrect: 0, empty: 0,
              };
            }

            if (studentAnswer === "-" || !studentAnswer) {
              topicPerformanceDelta[topicName].empty += 1;
            } else if (studentAnswer === correctAnswer) {
              topicPerformanceDelta[topicName].correct += 1;
            } else {
              topicPerformanceDelta[topicName].incorrect += 1;
            }
          }
        }

        const batch = db.batch();

        for (const topicName in topicPerformanceDelta) {
          if (Object.prototype.hasOwnProperty.call(
              topicPerformanceDelta, topicName)) {
            const delta = topicPerformanceDelta[topicName];
            const docId = `${studentId}_${topicName}`;
            const performanceDocRef =
              db.collection("userTopicPerformance").doc(docId);

            batch.set(performanceDocRef, {
              studentId: studentId,
              topicName: topicName,
              totalCorrect: admin.firestore.FieldValue.increment(delta.correct),
              totalIncorrect:
                admin.firestore.FieldValue.increment(delta.incorrect),
              totalEmpty: admin.firestore.FieldValue.increment(delta.empty),
            }, {merge: true});
          }
        }

        await batch.commit();
        console.log(`Performance updated for student ${studentId}.`);
        return null;
      } catch (error) {
        console.error("Error updating topic performance:", error);
        return null;
      }
    });

// =========================================================================
// FONKSİYON 1: YENİ BİR DENEME KAYDI OLUŞTURULDUĞUNDA EN YÜKSEK NETİ GÜNCELLE
// =========================================================================
exports.updateBestNetScoreOnCreate = functions.firestore
    .document("attempts/{attemptId}")
    .onCreate(async (snapshot) => {
      const attemptData = snapshot.data();
      if (!attemptData) return null;

      const {studentId, examId, booklet, answers} = attemptData;

      try {
        // --- 1. Yeni denemenin netini hesapla ---
        const examDocRef = db.collection("exams").doc(examId);
        const correctAnswersPromise = examDocRef
            .collection("booklets").doc(booklet)
            .collection("answerKey").get();
        const examDetailsPromise = examDocRef.get();

        const [correctAnswersSnapshot, examDetailsSnapshot] =
          await Promise.all([correctAnswersPromise, examDetailsPromise]);

        if (!examDetailsSnapshot.exists) return null;

        const correctAnswers = Object.fromEntries(
            correctAnswersSnapshot.docs.map((doc) => [doc.id, doc.data().correctAnswer])
        );
        const examType = examDetailsSnapshot.data().examType; // TYT, AYT vs.

        let correctCount = 0;
        let incorrectCount = 0;
        for (const qIndex in answers) {
          if (Object.prototype.hasOwnProperty.call(answers, qIndex)) {
            if (answers[qIndex] === correctAnswers[qIndex]) correctCount++;
            else if (answers[qIndex] !== "-") incorrectCount++;
          }
        }
        const newNet = correctCount - (incorrectCount / 4.0);

        // --- 2. Transaction ile rekoru güvenli bir şekilde güncelle ---
        const userDocRef = db.collection("users").doc(studentId);
        return db.runTransaction(async (transaction) => {
          const userDoc = await transaction.get(userDocRef);
          if (!userDoc.exists) return;

          const bestScores = userDoc.data().bestScores || {};
          const currentBestNet = bestScores[examType]?.net || -999;

          if (newNet > currentBestNet) {
            console.log(`New record for ${studentId} in ${examType}! Net: ${newNet}`);
            const newBestScore = {
              net: newNet, correct: correctCount, incorrect: incorrectCount,
            };
            transaction.update(userDocRef, {[`bestScores.${examType}`]: newBestScore});
          }
        });
      } catch (error) {
        console.error("Error in updateBestNetScoreOnCreate:", error);
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
        const userDocRef = db.collection("users").doc(studentId);
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