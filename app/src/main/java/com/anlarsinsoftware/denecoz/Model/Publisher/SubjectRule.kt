package com.anlarsinsoftware.denecoz.Model.Publisher

 data class SubjectRule(
    val subjectId: String,          // Veritabanındaki ders ID'si (örn: "din_kulturu")
    val questionCount: Int,         // Bu derse ait soru sayısı
    val isAlternative: Boolean = false // Bu dersin alternatif olup olmadığını belirtir
)
