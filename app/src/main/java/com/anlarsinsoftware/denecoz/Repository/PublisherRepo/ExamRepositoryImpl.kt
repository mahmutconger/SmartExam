package com.anlarsinsoftware.denecoz.Repository.PublisherRepo
import android.util.Log
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.Model.Publisher.FullExamData
import com.anlarsinsoftware.denecoz.Model.State.Publisher.BookletStatus
import com.anlarsinsoftware.denecoz.Model.State.Publisher.SubjectDef
import com.anlarsinsoftware.denecoz.Model.Student.AnalysisData
import com.anlarsinsoftware.denecoz.Model.Student.HistoricalTopicPerformance
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

    override suspend fun saveStudentAttempt(
        examId: String,
        bookletChoice: String,
        answers: Map<Int, Int?>,
        alternativeChoice: String?
    ): Result<String> {
        return try {

            val studentId = auth.currentUser?.uid ?: return Result.failure(Exception("Kullanıcı girişi yapılmamış."))
            val answersToSave = answers.mapKeys { it.key.toString() }
                .mapValues {
                    if (it.value == null) "-" else ('A' + it.value!!).toString()
                }

            val attemptData = mapOf(
                "studentId" to studentId,
                "examId" to examId,
                "completedAt" to FieldValue.serverTimestamp(),
                "booklet" to bookletChoice,
                "answers" to answersToSave,
                "alternativeChoice" to alternativeChoice
            )

            val docRef = firestore.collection("attempts").add(attemptData).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPublishedExams(): Result<List<PublishedExamSummary>> {
        return try {

            val querySnapshot = examsCollection
                .whereEqualTo("status", "published")
                .orderBy("createdAt", Query.Direction.DESCENDING) // En yeni denemeler üstte
                .get()
                .await()


            // TODO: Bu kısım publisher bilgilerini de çekmek için geliştirilebilir.
            val exams = querySnapshot.map { document ->
                PublishedExamSummary(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    coverImageUrl = document.getString("coverImageUrl"), // Bu alanı daha sonra ekleyeceğiz
                    publisherName = "Yayıncı Adı", // Şimdilik sabit
                    examType = document.getString("examType") ?: ""
                )
            }

            Result.success(exams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnalysisData(examId: String, attemptId: String): Result<AnalysisData> = coroutineScope {
        return@coroutineScope try {
            // 1. Önce öğrencinin deneme kaydını çekelim. Bu bize hangi kitapçığı çözdüğünü söyleyecek.
            val attemptSnapshot = firestore.collection("attempts").document(attemptId).get().await()
            if (!attemptSnapshot.exists()) {
                return@coroutineScope Result.failure(Exception("Deneme kaydı bulunamadı."))
            }

            // Deneme kaydından kritik bilgileri alalım
            val studentAnswers = attemptSnapshot.get("answers") as? Map<String, String> ?: emptyMap()
            val bookletChoice = attemptSnapshot.getString("booklet") ?: return@coroutineScope Result.failure(Exception("Kitapçık seçimi bulunamadı."))
            val studentAlternativeChoice = attemptSnapshot.getString("alternativeChoice")

            // 2. Artık kitapçığı bildiğimize göre, diğer tüm verileri ASENKRON (paralel) olarak çekebiliriz.
            val examDetailsDeferred = async { getExamDetails(examId) } // Mevcut fonksiyonumuzu yeniden kullanıyoruz!
            val correctAnswersDeferred = async {
                firestore.collection("exams").document(examId)
                    .collection("booklets").document(bookletChoice)
                    .collection("answerKey").get().await()
            }
            val topicDistDeferred = async {
                firestore.collection("exams").document(examId)
                    .collection("booklets").document(bookletChoice)
                    .collection("topicDistribution").get().await()
            }

            // 3. Tüm asenkron işlerin bitmesini bekleyelim.
            val examDetailsResult = examDetailsDeferred.await()
            val correctAnswersSnapshot = correctAnswersDeferred.await()
            val topicDistSnapshot = topicDistDeferred.await()

            // getExamDetails hata döndürdüyse, işlemi durduralım.
            if (examDetailsResult.isFailure) {
                return@coroutineScope Result.failure(examDetailsResult.exceptionOrNull()!!)
            }
            val examDetails = examDetailsResult.getOrThrow()

            // 4. Gelen verileri temiz Map'lere dönüştürelim.
            val correctAnswers = correctAnswersSnapshot.documents.associate { it.id to (it.getString("correctAnswer") ?: "") }
            val topicDistribution = topicDistSnapshot.documents.associate { it.id to (it.getString("topicId") ?: "") }

            // 5. Tüm verileri tek bir pakette toplayalım.
            val analysisData = AnalysisData(
                examDetails = examDetails,
                studentAnswers = studentAnswers,
                correctAnswers = correctAnswers,
                topicDistribution = topicDistribution,
                studentAlternativeChoice = studentAlternativeChoice
            )

            Result.success(analysisData)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getHistoricalTopicPerformance(studentId: String, topicName: String): Result<HistoricalTopicPerformance> {
        return try {
            val docId = "${studentId}_${topicName}"
            val docSnapshot = firestore.collection("userTopicPerformance").document(docId).get().await()

            if (docSnapshot.exists()) {
                val performance = docSnapshot.toObject(HistoricalTopicPerformance::class.java)!!
                Result.success(performance)
            } else {
                Result.success(HistoricalTopicPerformance(0, 0, 0))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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