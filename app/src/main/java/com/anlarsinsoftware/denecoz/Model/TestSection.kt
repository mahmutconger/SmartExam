package com.anlarsinsoftware.denecoz.Model

data class TestSection(
    val name: String,
    val questionCount: Int,
    val subSubjects: List<Map<String, Any>>?
)