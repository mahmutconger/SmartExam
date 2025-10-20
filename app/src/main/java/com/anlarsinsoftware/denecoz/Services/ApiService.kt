package com.anlarsinsoftware.denecoz.Services

// Network/ApiService.kt
import com.anlarsinsoftware.denecoz.Model.ApiResponse
import com.anlarsinsoftware.denecoz.Model.Province
import retrofit2.http.GET
import retrofit2.http.Query // Sorgu parametreleri için (ileride gerekirse)

interface ApiService {
    @GET("v1/provinces")
    suspend fun getAllProvinces(): ApiResponse<List<Province>>

    // İleride tek bir ili ID ile çekmek gerekirse:
    // @GET("v1/provinces/{id}")
    // suspend fun getProvinceById(@Path("id") provinceId: Int): ApiResponse<Province>
}