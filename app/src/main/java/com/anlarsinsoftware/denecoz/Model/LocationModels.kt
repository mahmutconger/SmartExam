package com.anlarsinsoftware.denecoz.Model

data class ApiResponse<T>(
    val status: String,
    val data: T
)

data class District(
    val id: Int,
    val name: String
)

data class Province(
    val id: Int,
    val name: String,
    val districts: List<District>
)