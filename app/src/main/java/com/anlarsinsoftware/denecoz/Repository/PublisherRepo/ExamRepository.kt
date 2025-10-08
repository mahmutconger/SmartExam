package com.anlarsinsoftware.denecoz.Repository.PublisherRepo

import com.anlarsinsoftware.denecoz.Model.Publisher.FullExamData
import com.anlarsinsoftware.denecoz.Model.State.SubjectDef
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ExamDetails(
    val name: String = "",
    val examType: String = "",
    val subjects: List<Map<String, Any>> = emptyList()
)

data class ExamSummary(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

interface ExamRepository {
    suspend fun createDraftExam(examDetails: Map<String, Any>): Result<String>
    suspend fun getExamDetails(examId: String): Result<ExamDetails>
    suspend fun saveAnswerKey(examId: String, booklet: String, answers: Map<String, String>): Result<Unit>
    suspend fun saveTopicDistribution(examId: String, booklet: String, topics: Map<String, String>): Result<Unit>
    suspend fun publishExam(examId: String): Result<Unit>
    suspend fun getExamsForPublisher(publisherId: String): Result<List<ExamSummary>>
    suspend fun getFullExamForPreview(examId: String): Result<FullExamData>
    suspend fun getCurriculum(examType: String): Result<List<SubjectDef>>
    suspend fun moveDocument(): Result<Unit>

}