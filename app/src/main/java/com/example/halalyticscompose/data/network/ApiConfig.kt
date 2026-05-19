package com.example.halalyticscompose.data.network

import android.util.Log
import com.example.halalyticscompose.BuildConfig
import com.example.halalyticscompose.data.api.ApiService
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

object ApiConfig {
    
    // ⚠️ IMPORTANT: Change based on your setup
    // For Android Emulator: use 10.0.2.2
    // For Real Device: use your computer's IP address (e.g., 192.168.1.100)
    // For Production: use your domain (e.g., https://api.halalytics.com/api/)
    
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/api/"
    private const val TAG = "HalalyticsApiConfig"
    
    // Alternative URLs (uncomment as needed):
    // private const val BASE_URL = "http://192.168.1.100:8000/api/" // For real device
    // private const val BASE_URL = "https://your-domain.com/api/" // For production
    
    /**
     * Create OkHttpClient with logging interceptor and JSON headers
     */
    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val requestMetricsInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestId = UUID.randomUUID().toString().take(8)
            val startNs = System.nanoTime()

            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")

            val contentType = original.header("Content-Type")?.lowercase().orEmpty()
            if (original.body != null &&
                contentType.isEmpty() &&
                !contentType.contains("multipart")
            ) {
                requestBuilder.header("Content-Type", "application/json")
            }

            val request = requestBuilder
                .header("X-Request-Id", requestId)
                .method(original.method, original.body)
                .build()

            try {
                val response = chain.proceed(request)
                val tookMs = (System.nanoTime() - startNs) / 1_000_000
                if (!response.isSuccessful) {
                    val errorBody = response.peekBody(1024).string().replace("\n", " ").trim()
                    Log.e(TAG, "[$requestId] ${request.method} ${request.url} -> ${response.code} (${tookMs}ms) error=$errorBody")
                } else {
                    Log.i(TAG, "[$requestId] ${request.method} ${request.url} -> ${response.code} (${tookMs}ms)")
                }
                response
            } catch (e: Exception) {
                val tookMs = (System.nanoTime() - startNs) / 1_000_000
                Log.e(TAG, "[$requestId] ${request.method} ${request.url} failed (${tookMs}ms): ${e.message}", e)
                throw e
            }
        }
        
        val builder = OkHttpClient.Builder()
            .addInterceptor(requestMetricsInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // Optional SSL pinning when API_BASE_URL is HTTPS and API_CERT_PIN is provided.
        val baseUrl = BuildConfig.API_BASE_URL.ifBlank { DEFAULT_BASE_URL }
        val host = baseUrl.toHttpUrlOrNull()?.host
        val pin = BuildConfig.API_CERT_PIN
        if (!host.isNullOrBlank() && pin.isNotBlank() && baseUrl.startsWith("https://")) {
            builder.certificatePinner(
                CertificatePinner.Builder()
                    .add(host, pin)
                    .build()
            )
        }

        return builder.build()
    }
    
    /**
     * Create Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        val baseUrl = BuildConfig.API_BASE_URL.ifBlank { DEFAULT_BASE_URL }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Main API Service
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // OCR Product API Service
    val ocrProductApiService: com.example.halalyticscompose.data.api.OCRProductApiService by lazy {
        retrofit.create(com.example.halalyticscompose.data.api.OCRProductApiService::class.java)
    }
    
    /**
     * Get Ingredient API Service
     */
    fun getIngredientApiService(): com.example.halalyticscompose.data.api.IngredientApiService {
        return retrofit.create(com.example.halalyticscompose.data.api.IngredientApiService::class.java)
    }
    
    /**
     * Create authenticated client with Bearer token
     */
    fun getAuthenticatedApiService(token: String): com.example.halalyticscompose.data.network.ExternalApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        
        val client = provideOkHttpClient().newBuilder()
            .addInterceptor(authInterceptor)
            .build()
        
        val authenticatedRetrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL.ifBlank { DEFAULT_BASE_URL })
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return authenticatedRetrofit.create(com.example.halalyticscompose.data.network.ExternalApiService::class.java)
    }

    /**
     * Get External API Service (Unauthenticated)
     */
    fun getExternalApiService(): com.example.halalyticscompose.data.network.ExternalApiService {
        return retrofit.create(com.example.halalyticscompose.data.network.ExternalApiService::class.java)
    }

    /**
     * Get OpenBeautyFacts API Service (public OpenBeautyFacts endpoints)
     */
    fun getOpenBeautyFactsApiService(): com.example.halalyticscompose.data.api.OpenBeautyFactsApiService {
        val retrofitOpenBeautyFacts = Retrofit.Builder()
            .baseUrl("https://world.openbeautyfacts.org/")
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofitOpenBeautyFacts.create(com.example.halalyticscompose.data.api.OpenBeautyFactsApiService::class.java)
    }

    /**
     * Get OpenFoodFacts public API service (direct fallback when backend external bridge fails).
     */
    fun getOpenFoodFactsApiService(): com.example.halalyticscompose.data.api.OpenFoodFactsApiService {
        val retrofitOpenFoodFacts = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofitOpenFoodFacts.create(com.example.halalyticscompose.data.api.OpenFoodFactsApiService::class.java)
    }

    /**
     * Get full image URL from relative path
     */
    fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrEmpty()) return null
        if (relativePath.startsWith("http")) return relativePath
        
        // Ensure starting slash
        val cleanPath = relativePath.trim()
        val path = if (cleanPath.startsWith("/")) cleanPath else "/$cleanPath"
        return "${getBaseDomain()}$path"
    }

    private fun getBaseDomain(): String {
        val url = BuildConfig.API_BASE_URL.ifBlank { DEFAULT_BASE_URL }.toHttpUrlOrNull()
            ?: return "http://10.0.2.2:8000"
        val includePort = !isDefaultPort(url)
        return buildString {
            append(url.scheme)
            append("://")
            append(url.host)
            if (includePort) {
                append(":")
                append(url.port)
            }
        }
    }

    private fun isDefaultPort(url: HttpUrl): Boolean {
        return (url.scheme == "http" && url.port == 80) ||
            (url.scheme == "https" && url.port == 443)
    }
}
