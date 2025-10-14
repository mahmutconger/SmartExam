const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
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