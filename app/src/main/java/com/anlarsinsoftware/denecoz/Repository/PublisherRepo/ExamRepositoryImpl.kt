package com.anlarsinsoftware.denecoz.Repository.PublisherRepo
import com.anlarsinsoftware.denecoz.Model.Publisher.FullExamData
import com.anlarsinsoftware.denecoz.Model.State.SubjectDef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ExamRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExamRepository {

    private val examsCollection = firestore.collection("exams")

    override suspend fun createDraftExam(examDetails: Map<String, Any>): Result<String> {
        return try {
            val publisherId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Giriş yapmış bir yayıncı bulunamadı."))

            val fullExamData = examDetails.toMutableMap().apply {
                put("publisherId", publisherId)
                put("status", "draft")
                put("createdAt", FieldValue.serverTimestamp())
            }

            val documentReference = examsCollection.add(fullExamData).await()
            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExamDetails(examId: String): Result<ExamDetails> {
        return try {
            val snapshot = examsCollection.document(examId).get().await()
            val examDetails = snapshot.toObject(ExamDetails::class.java)
                ?: return Result.failure(Exception("Deneme detayları bulunamadı."))
            Result.success(examDetails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurriculum(examType: String): Result<List<SubjectDef>> {
        return try {
            val subjectDefs = mutableListOf<SubjectDef>()

            // examType'a ait dersleri çek (örn: TYT'nin altındaki 'subjects' koleksiyonu)
            val subjectsSnapshot = firestore.collection("curriculum").document(examType)
                .collection("subjects").get().await()

            for (subjectDoc in subjectsSnapshot.documents) {
                val subjectName = subjectDoc.getString("name") ?: ""

                // Her dersin altındaki konuları çek
                val topicsSnapshot = subjectDoc.reference.collection("topics").get().await()
                val topicsList = topicsSnapshot.documents.map { it.getString("name") ?: "" }

                subjectDefs.add(
                    SubjectDef(
                        name = subjectName,
                        totalQuestions = 0, // Bu bilgiyi examDetails'den alacağız
                        topics = topicsList
                    )
                )
            }
            Result.success(subjectDefs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFullExamForPreview(examId: String): Result<FullExamData> = coroutineScope {
        try {
            val examDetailsDeferred = async { getExamDetails(examId) }
            val answerKeyDeferred = async {
                examsCollection.document(examId)
                    .collection("booklets").document("A")
                    .collection("answerKey")
                    .get().await()
            }
            val topicDistributionDeferred = async {
                examsCollection.document(examId)
                    .collection("booklets").document("A")
                    .collection("topicDistribution")
                    .get().await()
            }

            val examDetailsResult = examDetailsDeferred.await()
            val answerKeySnapshot = answerKeyDeferred.await()
            val topicDistributionSnapshot = topicDistributionDeferred.await()

            if (examDetailsResult.isFailure) {
                Result.failure(examDetailsResult.exceptionOrNull() ?: Exception("Deneme detayları alınamadı."))
            } else {
                val answerKeyMap = answerKeySnapshot.documents.associate { it.id to (it.getString("correctAnswer") ?: "") }
                val topicDistributionMap = topicDistributionSnapshot.documents.associate { it.id to (it.getString("topicId") ?: "") }
                val fullExamData = FullExamData(
                    details = examDetailsResult.getOrThrow(),
                    answerKey = answerKeyMap,
                    topicDistribution = topicDistributionMap
                )

                Result.success(fullExamData)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveAnswerKey(examId: String, booklet: String, answers: Map<String, String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val answerKeyCollection = examsCollection.document(examId)
                .collection("booklets").document(booklet)
                .collection("answerKey")

            answers.forEach { (questionNumber, answer) ->
                val docRef = answerKeyCollection.document(questionNumber)
                val data = mapOf("correctAnswer" to answer)
                batch.set(docRef, data)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTopicDistribution(examId: String, booklet: String, topics: Map<String, String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val topicCollection = examsCollection.document(examId)
                .collection("booklets").document(booklet)
                .collection("topicDistribution")

            topics.forEach { (questionNumber, topic) ->
                val docRef = topicCollection.document(questionNumber)
                val data = mapOf("topicId" to topic) // Şimdilik sadece topicId saklıyoruz
                batch.set(docRef, data)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishExam(examId: String): Result<Unit> {
        return try {
            examsCollection.document(examId).update("status", "published").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExamsForPublisher(publisherId: String): Result<List<ExamSummary>> {
        return try {
            val querySnapshot = examsCollection
                .whereEqualTo("publisherId", publisherId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val exams = querySnapshot.map { document ->
                document.toObject(ExamSummary::class.java).copy(id = document.id)
            }
            Result.success(exams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}