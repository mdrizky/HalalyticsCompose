package com.example.halalyticscompose.data.api

import com.example.halalyticscompose.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Endpoint sinkronisasi offline (WorkManager). Panggilan produk utama ada di [ApiService].
 */
interface ProductApiService {

    @POST("sync/scan-logs")
    suspend fun syncScanLogs(
        @Header("Authorization") token: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<Any>>
}

// (HalalAlternativeResponse and HalalAlternativeItem moved to HalalModels.kt to avoid redeclaration)

