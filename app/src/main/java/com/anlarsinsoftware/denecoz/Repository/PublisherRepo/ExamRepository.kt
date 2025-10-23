package com.anlarsinsoftware.denecoz.Repository.PublisherRepo

import android.net.Uri
import com.anlarsinsoftware.denecoz.Model.PublishedExamSummary
import com.anlarsinsoftware.denecoz.Model.Publisher.FullExamData
import com.anlarsinsoftware.denecoz.Model.State.Publisher.BookletStatus
import com.anlarsinsoftware.denecoz.Model.State.Publisher.PublisherProfile
import com.anlarsinsoftware.denecoz.Model.State.Publisher.SubjectDef
import com.anlarsinsoftware.denecoz.Model.State.Student.BestNetResult
import com.anlarsinsoftware.denecoz.Model.State.Student.LeaderboardEntry
import com.anlarsinsoftware.denecoz.Model.State.Student.PastAttemptSummary
import com.anlarsinsoftware.denecoz.Model.State.Student.UserProfile
import com.anlarsinsoftware.denecoz.Model.Student.AnalysisData
import com.anlarsinsoftware.denecoz.Model.Student.HistoricalTopicPerformance
import com.anlarsinsoftware.denecoz.Model.Student.NetScoreHistoryPoint
import com.anlarsinsoftware.denecoz.Model.Student.PublisherSummary
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ExamDetails(
    val name: String = "",
    val examType: String = "",
    // bookletType'ı GeneralInfo'dan kaldırdığımız için buradan da kaldırabiliriz,
    // artık bookletStatus haritasında yönetiliyor.
    // val bookletType: String = "",
    val publicationDate: String = "",
    val subjects: List<Map<String, Any>> = emptyList(),
    val publisherId: String = "",
    val publisherName: String = "",
    val coverImageUrl: String? = null,
    val difficulty: String = "Orta",
    val solveCount: Long = 0,
    val status: String = "draft"
    // ... (createdAt vb. eklenebilir)
)

data class ExamStatus(
    val hasAnswerKey: Boolean,
    val hasTopicDistribution: Boolean
)

data class ExamSummary(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    val coverImageUrl : String? = null,
    @ServerTimestamp val createdAt: Date? = null
)

interface ExamRepository {
    suspend fun createDraftExam(examDetails: Map<String, Any>, imageUri: Uri?): Result<String>
    suspend fun getExamDetails(examId: String): Result<ExamDetails>
    suspend fun saveAnswerKey(examId: String, booklet: String, answers: Map<String, String>): Result<Unit>
    suspend fun saveTopicDistribution(examId: String, booklet: String, topics: Map<String, Map<String, String>>): Result<Unit>
    suspend fun publishExam(examId: String): Result<Unit>
    suspend fun getPublisherProfile(publisherId: String): Result<PublisherProfile>
    suspend fun getExamsForPublisher(publisherId: String): Result<List<ExamSummary>>
    suspend fun getFullExamForPreview(examId: String): Result<FullExamData>
    suspend fun getCurriculum(examType: String): Result<List<SubjectDef>>
    suspend fun moveDocument(oldCollectionPath: String, oldDocId: String, newCollectionPath: String, newDocId: String): Result<Unit>
    suspend fun getExamStatus(examId: String): Result<Map<String, BookletStatus>>
    suspend fun addNewBooklet(examId: String, bookletName: String): Result<Unit>
    suspend fun getPublishedExams(): Result<List<PublishedExamSummary>>
    suspend fun saveStudentAttempt(examId: String, bookletChoice: String, answers: Map<Int, Int?>, alternativeChoice: String?): Result<String>
    suspend fun getAnalysisData(examId: String, attemptId: String): Result<AnalysisData>
    suspend fun getHistoricalTopicPerformance(studentId: String, uniqueTopicId: String): Result<HistoricalTopicPerformance>
    suspend fun getUserProfile(studentId: String): Result<UserProfile>
    suspend fun getPastAttempts(studentId: String): Result<List<PastAttemptSummary>>
    suspend fun getPublishers(): Result<List<PublisherSummary>>
    suspend fun hasUserSolvedExam(studentId: String, examId: String): Result<Boolean>
    suspend fun getLeaderboard(
        examId: String,
        scope: String, // "turkey", "province", "district"
        city: String? = null,
        district: String? = null,
        limit: Long = 50
    ): Result<List<LeaderboardEntry>>
    suspend fun getMyLeaderboardEntry(examId: String, studentId: String): Result<LeaderboardEntry?>
    suspend fun getNetScoreHistory(
        studentId: String,
        examType: String,
        startDate: Date
    ): Result<List<NetScoreHistoryPoint>>
}