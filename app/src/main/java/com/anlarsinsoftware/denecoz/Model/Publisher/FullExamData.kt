package com.anlarsinsoftware.denecoz.Model.Publisher

import com.anlarsinsoftware.denecoz.Repository.PublisherRepo.ExamDetails

data class FullExamData(
    val details: ExamDetails,
    val answerKeys: Map<String, Map<String, String>>,
    val topicDistributions: Map<String, Map<String, String>>
)