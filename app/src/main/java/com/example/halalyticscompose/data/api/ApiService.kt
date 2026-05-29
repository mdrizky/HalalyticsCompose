package com.example.halalyticscompose.data.api

import com.example.halalyticscompose.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @FormUrlEncoded
    @POST("auth/google")
    suspend fun googleLogin(
        @Field("id_token") idToken: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("auth/facebook")
    suspend fun facebookLogin(
        @Field("access_token") accessToken: String
    ): Response<LoginResponse>

    @GET("user/profile")
    suspend fun getProfile(
        @Header("Authorization") bearer: String
    ): Response<ProfileResponse>

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

    @POST("user/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    ): GenericResponse

    @FormUrlEncoded
    @POST("forgot-password")
    suspend fun forgotPassword(
        @Field("email") email: String
    ): GenericResponse

    @FormUrlEncoded
    @POST("user/change-password")
    suspend fun changePassword(
        @Header("Authorization") bearer: String,
        @Field("current_password") current: String,
        @Field("new_password") new: String,
        @Field("new_password_confirmation") confirm: String
    ): GenericResponse

    // ==================== ADMIN ====================
    @GET("admin/dashboard/stats")
    suspend fun getDashboardStats(
        @Header("Authorization") bearer: String
    ): DashboardStatsResponse

    @GET("admin/products/pending")
    suspend fun getPendingProducts(
        @Header("Authorization") bearer: String
    ): PendingProductsResponse

    @PUT("admin/products/{id}/approve")
    suspend fun approveProduct(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): ApprovalResponse

    @PUT("admin/products/{id}/reject")
    suspend fun rejectProduct(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Body reason: Map<String, String>
    ): ApprovalResponse

    @GET("admin/users")
    suspend fun getAllUsers(
        @Header("Authorization") bearer: String
    ): UserListResponse

    @PUT("admin/users/{id}")
    suspend fun updateUserByAdmin(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Body data: Map<String, @JvmSuppressWildcards Any?>
    ): GenericResponse

    // ==================== SCAN & PRODUCTS ====================
    @GET("products/scan/{barcode}")
    suspend fun scanUnified(
        @Header("Authorization") bearer: String,
        @Path("barcode") barcode: String
    ): Response<UnifiedScanResponse>

    @GET("products/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): Response<ApiResponse<ProductDetailData>>

    @GET("https://world.openfoodfacts.org/api/v0/product/{barcode}.json")
    suspend fun getOpenFoodFactsProduct(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>

    @POST("halal/check")
    suspend fun checkHalal(
        @Body request: HalalCheckRequest
    ): Response<HalalCheckResponse>

    @GET("products/{barcode}/alternatives")
    suspend fun getProductAlternatives(
        @Path("barcode") barcode: String,
        @Header("Authorization") bearer: String
    ): Response<ApiResponse<HalalAlternativeResponse>>

    // ==================== MEDICINE ====================
    @GET("user/medicine-reminders")
    suspend fun getUserMedicineReminders(
        @Header("Authorization") bearer: String
    ): Response<ApiResponse<List<MedicineReminder>>>

    @POST("user/medicine-reminders/{id}/taken")
    suspend fun markMedicineAsTaken(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): Response<GenericResponse>

    // ==================== NUTRITION & HEALTH ====================
    @POST("user/nutrition/log")
    suspend fun logMeal(
        @Header("Authorization") bearer: String,
        @Body request: MealLogRequest
    ): Response<ApiResponse<DailyNutritionLog>>

    @Multipart
    @POST("user/nutrition/log")
    suspend fun logMealMultipart(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part,
        @Part("meal_type") mealType: okhttp3.RequestBody
    ): Response<ApiResponse<DailyNutritionLog>>

    @GET("user/nutrition/daily")
    suspend fun getDailyNutritionLog(
        @Header("Authorization") bearer: String,
        @Query("date") date: String? = null
    ): Response<ApiResponse<NutritionDashboardData>>

    @GET("user/nutrition/history")
    suspend fun getNutritionHistory(
        @Header("Authorization") bearer: String,
        @Query("days") days: Int = 30
    ): Response<ApiResponse<List<NutritionHistoryItem>>>

    @POST("user/nutrition/goals")
    suspend fun setNutritionGoals(
        @Header("Authorization") bearer: String,
        @Body request: NutritionGoal
    ): Response<GenericResponse>

    // ==================== AI & OCR ====================
    @POST("ai/analyze-ingredients")
    suspend fun analyzeIngredients(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, String>
    ): Response<ApiResponse<AiAnalysisData>>

    @POST("ocr/sync")
    suspend fun syncIngredients(
        @Header("Authorization") bearer: String,
        @Query("updated_after") updatedAfter: String? = null
    ): Response<OcrSyncResponse>

    @POST("ocr/save")
    suspend fun saveOcrResult(
        @Header("Authorization") bearer: String,
        @Body request: OcrScanResultRequest
    ): Response<GenericResponse>

    // ==================== HEALTH DATA ====================
    @GET("health/intake/daily")
    suspend fun getDailyIntake(
        @Header("Authorization") bearer: String
    ): Response<DailyIntakeResponse>

    @GET("health/insight/daily")
    suspend fun getAiDailyInsight(
        @Header("Authorization") bearer: String
    ): Response<AiInsightResponse>

    @GET("health/score")
    suspend fun getHealthScore(
        @Header("Authorization") bearer: String
    ): Response<HealthScoreResponse>

    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    // ==================== EXTERNAL SEARCH ====================
    @GET("https://api.unsplash.com/search/photos")
    suspend fun searchUnsplashPhotos(
        @Query("query") query: String,
        @Header("Authorization") clientId: String,
        @Query("per_page") perPage: Int = 5
    ): Response<UnsplashSearchResponse>
}
