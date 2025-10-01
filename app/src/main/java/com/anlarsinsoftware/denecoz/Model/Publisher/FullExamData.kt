package com.anlarsinsoftware.denecoz.Model.Publisher

import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamDetails

data class FullExamData(
    val details: ExamDetails,
    val answerKey: Map<String, String>,
    val topicDistribution: Map<String, String>
)