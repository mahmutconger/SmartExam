package com.anlarsinsoftware.denecoz.Repository.PublisherRepo
import com.anlarsinsoftware.denecoz.Model.Publisher.FullExamData
import com.anlarsinsoftware.denecoz.Model.State.BookletStatus
import com.anlarsinsoftware.denecoz.Model.State.SubjectDef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
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
                put("bookletStatus", mapOf(
                    "A" to mapOf("hasAnswerKey" to false, "hasTopicDistribution" to false)
                ))
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
                        id = subjectDoc.id,
                        name = subjectName,
                        totalQuestions = 0,
                        topics = topicsList
                    )
                )
            }
            Result.success(subjectDefs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExamStatus(examId: String): Result<Map<String, BookletStatus>> {
        return try {
            val document = examsCollection.document(examId).get().await()

            val statusMap = document.get("bookletStatus") as? Map<String, Map<String, Boolean>> ?: emptyMap()

            val result = statusMap.mapValues { (_, value) ->
                BookletStatus(
                    hasAnswerKey = value["hasAnswerKey"] ?: false,
                    hasTopicDistribution = value["hasTopicDistribution"] ?: false
                )
            }
            Result.success(result)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getFullExamForPreview(examId: String): Result<FullExamData> = coroutineScope {
        try {
            val examDocSnapshot = examsCollection.document(examId).get().await()
            val examDetails = examDocSnapshot.toObject(ExamDetails::class.java)
                ?: return@coroutineScope Result.failure(Exception("Deneme detayları bulunamadı."))

            val bookletStatusMap = examDocSnapshot.get("bookletStatus") as? Map<String, Any>
            val bookletNames = bookletStatusMap?.keys?.toList() ?: emptyList()

            val answerKeyDeferreds = bookletNames.associateWith { bookletName ->
                async(Dispatchers.IO) {
                    examsCollection.document(examId)
                        .collection("booklets").document(bookletName)
                        .collection("answerKey").get().await()
                }
            }
            val topicDistDeferreds = bookletNames.associateWith { bookletName ->
                async(Dispatchers.IO) {
                    examsCollection.document(examId)
                        .collection("booklets").document(bookletName)
                        .collection("topicDistribution").get().await()
                }
            }

            val answerKeys = answerKeyDeferreds.mapValues { (_, deferred) ->
                deferred.await().documents.associate { it.id to (it.getString("correctAnswer") ?: "") }
            }
            val topicDistributions = topicDistDeferreds.mapValues { (_, deferred) ->
                deferred.await().documents.associate { it.id to (it.getString("topicId") ?: "") }
            }

            val fullExamData = FullExamData(
                details = examDetails,
                answerKeys = answerKeys,
                topicDistributions = topicDistributions
            )

            Result.success(fullExamData)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    private suspend fun copyDocumentRecursively(oldDocRef: DocumentReference, newDocRef: DocumentReference) {
        // 1. Ana dokümanın verisini kopyala
        val oldDocSnapshot = oldDocRef.get().await()
        if (oldDocSnapshot.exists()) {
            newDocRef.set(oldDocSnapshot.data!!).await()
        }

        // 2. 'topics' alt koleksiyonunu manuel olarak kopyala
        val oldTopicsCollection = oldDocRef.collection("topics").get().await()
        for (oldTopicDoc in oldTopicsCollection.documents) {
            val newTopicDocRef = newDocRef.collection("topics").document(oldTopicDoc.id)
            // 'topics' dokümanının kendi verisini kopyala
            newTopicDocRef.set(oldTopicDoc.data!!).await()

            // 3. 'topics' altındaki 'subTopics' koleksiyonunu kopyala
            val oldSubTopicsCollection = oldTopicDoc.reference.collection("subTopics").get().await()
            for (oldSubTopicDoc in oldSubTopicsCollection.documents) {
                val newSubTopicDocRef = newTopicDocRef.collection("subTopics").document(oldSubTopicDoc.id)
                newSubTopicDocRef.set(oldSubTopicDoc.data!!).await()
            }
        }
    }

    // Bu fonksiyon artık düzeltilmiş `copyDocumentRecursively` fonksiyonunu kullanıyor.
    override suspend fun moveDocument(oldCollectionPath: String, oldDocId: String, newCollectionPath: String, newDocId: String): Result<Unit> {
        return try {
            val oldDocRef = firestore.collection(oldCollectionPath).document(oldDocId)
            val newDocRef = firestore.collection(newCollectionPath).document(newDocId)

            // Kopyalama işlemini başlat
            copyDocumentRecursively(oldDocRef, newDocRef)


            Result.success(Unit)
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
            val fieldToUpdate = "bookletStatus.$booklet.hasAnswerKey"
            examsCollection.document(examId).update(fieldToUpdate, true).await()

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
                val data = mapOf("topicId" to topic)
                batch.set(docRef, data)
            }

            batch.commit().await()

            val fieldToUpdate = "bookletStatus.$booklet.hasTopicDistribution"
            examsCollection.document(examId).update(fieldToUpdate, true).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addNewBooklet(examId: String, bookletName: String): Result<Unit> {
        return try {
            val examDocRef = examsCollection.document(examId)
            // Yeni kitapçık için başlangıç durumunu içeren bir harita oluştur
            val newBookletStatus = mapOf(
                "hasAnswerKey" to false,
                "hasTopicDistribution" to false
            )
            // Dot notation ile 'bookletStatus' haritasına yeni bir alan ekle
            val fieldToUpdate = "bookletStatus.$bookletName"
            examDocRef.update(fieldToUpdate, newBookletStatus).await()
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