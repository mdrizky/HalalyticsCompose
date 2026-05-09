package com.example.halalyticscompose.data.api

import com.example.halalyticscompose.data.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        @Body reason: Map<String, String> // {"reason": "..."}
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

    // ==================== USER ====================
    @GET("user/stats")
    suspend fun getUserStats(
        @Header("Authorization") bearer: String
    ): UserStatsResponse

    @FormUrlEncoded
    @POST("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") bearer: String,
        @Field("full_name") fullName: String? = null,
        @Field("email") email: String? = null,
        @Field("phone") phone: String? = null,
        @Field("blood_type") bloodType: String? = null,
        @Field("allergy") allergy: String? = null,
        @Field("medical_history") medicalHistory: String? = null,
        @Field("diet_preference") dietPreference: String? = null,
        @Field("age") age: Int? = null,
        @Field("height") height: Double? = null,
        @Field("weight") weight: Double? = null,
        @Field("goal") goal: String? = null,
        @Field("activity_level") activityLevel: String? = null,
        @Field("gender") gender: String? = null,
        @Field("bio") bio: String? = null,
        @Field("address") address: String? = null,
        @Field("emergency_contact") emergencyContact: String? = null,
        @Field("birth_date") birthDate: String? = null,
        @Field("language") language: String? = null
    ): LoginModel

    @Multipart
    @POST("user/profile")
    suspend fun updateProfileMultipart(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part?,
        @Part("full_name") fullName: okhttp3.RequestBody? = null,
        @Part("email") email: okhttp3.RequestBody? = null,
        @Part("phone") phone: okhttp3.RequestBody? = null,
        @Part("blood_type") bloodType: okhttp3.RequestBody? = null,
        @Part("allergy") allergy: okhttp3.RequestBody? = null,
        @Part("medical_history") medicalHistory: okhttp3.RequestBody? = null,
        @Part("diet_preference") dietPreference: okhttp3.RequestBody? = null,
        @Part("age") age: okhttp3.RequestBody? = null,
        @Part("height") height: okhttp3.RequestBody? = null,
        @Part("weight") weight: okhttp3.RequestBody? = null,
        @Part("goal") goal: okhttp3.RequestBody? = null,
        @Part("activity_level") activityLevel: okhttp3.RequestBody? = null,
        @Part("gender") gender: okhttp3.RequestBody? = null,
        @Part("bio") bio: okhttp3.RequestBody? = null,
        @Part("address") address: okhttp3.RequestBody? = null,
        @Part("emergency_contact") emergencyContact: okhttp3.RequestBody? = null,
        @Part("birth_date") birthDate: okhttp3.RequestBody? = null,
        @Part("language") language: okhttp3.RequestBody? = null
    ): LoginModel

    @FormUrlEncoded
    @POST("user/sync") // Updated from legacy sync_user.php
    suspend fun syncUser(
        @Field("firebase_uid") firebaseUid: String,
        @Field("email") email: String,
        @Field("display_name") displayName: String?,
        @Field("fcm_token") fcmToken: String?
    ): GenericResponse

    @FormUrlEncoded
    @POST("fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") bearer: String,
        @Field("fcm_token") fcmToken: String,
        @Field("device_type") deviceType: String = "android",
        @Field("device_id") deviceId: String? = null
    ): GenericResponse

    @GET("user/family")
    suspend fun getFamilyProfiles(
        @Header("Authorization") bearer: String
    ): FamilyListResponse

    @FormUrlEncoded
    @POST("user/family")
    suspend fun addFamilyProfile(
        @Header("Authorization") bearer: String,
        @Field("name") name: String,
        @Field("relationship") relationship: String? = null,
        @Field("age") age: Int? = null,
        @Field("gender") gender: String? = null,
        @Field("allergies") allergies: String? = null,
        @Field("medical_history") medicalHistory: String? = null
    ): FamilyDetailResponse

    @Multipart
    @POST("user/family")
    suspend fun addFamilyProfileMultipart(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part?,
        @Part("name") name: okhttp3.RequestBody,
        @Part("relationship") relationship: okhttp3.RequestBody? = null,
        @Part("age") age: okhttp3.RequestBody? = null,
        @Part("gender") gender: okhttp3.RequestBody? = null,
        @Part("allergies") allergies: okhttp3.RequestBody? = null,
        @Part("medical_history") medicalHistory: okhttp3.RequestBody? = null
    ): FamilyDetailResponse

    @FormUrlEncoded
    @POST("user/family/{id}")
    suspend fun updateFamilyProfile(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Field("_method") method: String = "PUT",
        @Field("name") name: String? = null,
        @Field("relationship") relationship: String? = null,
        @Field("age") age: Int? = null,
        @Field("gender") gender: String? = null,
        @Field("allergies") allergies: String? = null,
        @Field("medical_history") medicalHistory: String? = null
    ): FamilyDetailResponse

    @Multipart
    @POST("user/family/{id}")
    suspend fun updateFamilyProfileMultipart(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Part("_method") method: okhttp3.RequestBody, // Should be "PUT"
        @Part image: okhttp3.MultipartBody.Part?,
        @Part("name") name: okhttp3.RequestBody? = null,
        @Part("relationship") relationship: okhttp3.RequestBody? = null,
        @Part("age") age: okhttp3.RequestBody? = null,
        @Part("gender") gender: okhttp3.RequestBody? = null,
        @Part("allergies") allergies: okhttp3.RequestBody? = null,
        @Part("medical_history") medicalHistory: okhttp3.RequestBody? = null
    ): FamilyDetailResponse

    @DELETE("user/family/{id}")
    suspend fun deleteFamilyProfile(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): GenericResponse

    // ==================== SCANS (Legacy) ====================
    @GET("scans")
    suspend fun getScanHistory(
        @Header("Authorization") bearer: String
    ): ScanHistoryResponse

    @FormUrlEncoded
    @POST("scans")
    suspend fun storeScan(
        @Header("Authorization") bearer: String,
        @Field("product_id") productId: Int?,
        @Field("nama_produk") namaProduk: String,
        @Field("barcode") barcode: String?,
        @Field("kategori") kategori: String?,
        @Field("status_halal") statusHalal: String,
        @Field("status_kesehatan") statusKesehatan: String,
        @Field("tanggal_expired") tanggalExpired: String?
    ): ScanModel

    // ==================== PRODUCTS & HALAL (Legacy/Search) ====================
    @GET("products/barcode/{barcode}")
    suspend fun getProduct(
        @Path("barcode") barcode: String
    ): Response<HalalProductResponse>

    @GET("products/barcode/{barcode}")
    suspend fun searchByBarcode(
        @Header("Authorization") bearer: String,
        @Path("barcode") barcode: String
    ): Response<ApiResponse<ProductInfo>>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ProductResponse

    @GET("https://world.openfoodfacts.org/api/v0/product/{barcode}.json")
    suspend fun getOpenFoodFactsProduct(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>

    @GET("https://api.unsplash.com/search/photos")
    suspend fun searchUnsplashPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 4,
        @Query("orientation") orientation: String = "squarish",
        @Header("Authorization") clientId: String
    ): Response<com.example.halalyticscompose.data.model.UnsplashSearchResponse>

    @POST("halal/check")
    suspend fun checkHalal(
        @Body request: HalalCheckRequest
    ): Response<HalalCheckResponse>

    // ==================== STREET FOOD & AI RECOGNITION ====================
    @GET("food/search")
    suspend fun searchFood(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): ApiResponse<List<StreetFood>>

    @GET("food/popular")
    suspend fun getPopularFoods(
        @Header("Authorization") token: String
    ): ApiResponse<List<StreetFood>>

    @POST("food/analyze")
    suspend fun analyzeFood(
        @Header("Authorization") token: String,
        @Body request: FoodAnalysisRequest
    ): ApiResponse<FoodAnalysis>



    // ==================== NOTIFICATIONS (Realtime) ====================
    @GET("notifications")
    suspend fun getNotifications(@Header("Authorization") token: String): NotificationResponse

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(@Header("Authorization") token: String): UnreadCountResponse

    @POST("notifications/{id}/read")
    suspend fun markAsRead(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>

    @POST("notifications/read-all")
    suspend fun markAllAsRead(@Header("Authorization") token: String): Response<Unit>


    // ==================== SCAN HISTORY (Realtime - New) ====================
    @GET("scan-history")
    suspend fun getRealtimeScanHistory(
        @Header("Authorization") token: String,
        @Query("period") period: String? = null,
        @Query("source") source: String? = null,
        @Query("page") page: Int? = 1
    ): RealtimeScanHistoryResponse

    @GET("scan-history/{id}")
    suspend fun getScanHistoryDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): ScanHistoryDetailResponse

    @POST("scan-history")
    suspend fun recordScan(
        @Header("Authorization") token: String,
        @Body request: RecordScanRequest
    ): Response<ScanResponse>

    @DELETE("scan-history/{id}")
    suspend fun deleteScanHistory(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>


    // ==================== FAVORITES (Realtime) ====================
    @GET("favorites")
    suspend fun getFavorites(@Header("Authorization") token: String): FavoriteResponse

    @POST("favorites")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Body request: AddFavoriteRequest
    ): Response<ApiResponse<FavoriteItem>>

    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>

    @FormUrlEncoded
    @PUT("favorites/{id}/notes")
    suspend fun updateFavoriteNotes(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("user_notes") notes: String
    ): Response<Unit>

    // ==================== DUAL SOURCE SCANNING ====================
    @FormUrlEncoded
    @POST("scan/unified")
    suspend fun scanUnified(
        @Header("Authorization") token: String,
        @Field("barcode") barcode: String
    ): Response<UnifiedScanResponse>

    // ==================== PREMIUM FEATURES ====================
    @GET("user/stats/weekly")
    suspend fun getWeeklyStats(
        @Header("Authorization") bearer: String
    ): WeeklyStatsResponse

    // AI MEAL SCANNER
    @POST("meal/analyze")
    suspend fun analyzeMeal(
        @Header("Authorization") token: String,
        @Body request: com.example.halalyticscompose.data.model.MealAnalysisRequest
    ): Response<com.example.halalyticscompose.data.model.MealAnalysisResponse>

    @FormUrlEncoded
    @POST("products/request-verification")
    suspend fun requestVerification(
        @Header("Authorization") bearer: String,
        @Field("barcode") barcode: String,
        @Field("product_name") productName: String? = null,
        @Field("notes") notes: String? = null
    ): GenericResponse

    @GET("products/recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") bearer: String,
        @Query("category") category: String
    ): RecommendationsResponse

    // ==================== AI ASSISTANT ====================
    @POST("ai/analyze")
    suspend fun analyzeIngredients(
        @Header("Authorization") bearer: String,
        @Body request: AiAnalysisRequest
    ): AiAnalysisResponse

    @GET("ai/weekly-report")
    suspend fun getWeeklyReport(
        @Header("Authorization") bearer: String,
        @Query("days") days: Int = 7
    ): AiReportResponse

    @GET("ai/personal-risk-score")
    suspend fun getPersonalRiskScore(
        @Header("Authorization") bearer: String,
        @Query("date") date: String? = null
    ): com.example.halalyticscompose.data.model.PersonalRiskScoreResponse

    @GET("ai/daily-intake")
    suspend fun getDailyIntake(@Header("Authorization") bearer: String): Response<DailyIntakeResponse>

    @GET("ai/daily-insight")
    suspend fun getAiDailyInsight(@Header("Authorization") bearer: String): Response<DailyInsightResponse>

    @GET("health/score")
    suspend fun getHealthScore(@Header("Authorization") bearer: String): Response<HealthScoreResponse>

    // ==================== HEALTH ENCYCLOPEDIA ====================
    @GET("health-encyclopedia")
    suspend fun getHealthEncyclopedia(
        @Query("type") type: String? = null,
        @Query("search") search: String? = null
    ): Response<HealthEncyclopediaResponse>

    @GET("health-encyclopedia/{id}")
    suspend fun getHealthEncyclopediaById(
        @Path("id") id: Int
    ): Response<HealthEncyclopediaDetailResponse>

    // ==================== PRODUCT REQUESTS (Crowdsourcing) ====================
    @Multipart
    @POST("product-requests")
    suspend fun uploadProductRequest(
        @Header("Authorization") bearer: String,
        @Part imageFront: okhttp3.MultipartBody.Part,
        @Part imageBack: okhttp3.MultipartBody.Part,
        @Part("barcode") barcode: okhttp3.RequestBody,
        @Part("product_name") productName: okhttp3.RequestBody,
        @Part("ocr_text") ocrText: okhttp3.RequestBody?
    ): Response<GenericResponse>

    // ==================== CONTRIBUTIONS ====================
    @Multipart
    @POST("contributions/submit")
    suspend fun submitContribution(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part?,
        @Part("product_name") productName: okhttp3.RequestBody,
        @Part("barcode") barcode: okhttp3.RequestBody?,
        @Part("ingredients") ingredients: okhttp3.RequestBody?
    ): ContributionResponse

    @GET("contributions/my")
    suspend fun getMyContributions(
        @Header("Authorization") bearer: String
    ): MyContributionsResponse


    // ==================== [LEGACY CERTIFICATES REMOVED] ====================


    // PHASE 5: MEDICINE
    @POST("medicines/check")
    suspend fun checkMedicine(
        @Header("Authorization") token: String,
        @Body request: Map<String, String> // {"name": "..."}
    ): Response<com.example.halalyticscompose.data.model.MedicineCheckResponse>

    @POST("medicines/schedule")
    suspend fun addMedicineSchedule(
        @Header("Authorization") token: String,
        @Body request: com.example.halalyticscompose.data.model.MedicineScheduleRequest
    ): Response<com.example.halalyticscompose.data.model.GenericResponse> // Generic success msg

    @GET("medicines/my")
    suspend fun getMyMedicines(
        @Header("Authorization") token: String
    ): Response<com.example.halalyticscompose.data.model.UserRemindersResponse>

    // AI HEALTH ASSISTANT - NEW METHODS
    @FormUrlEncoded
    @POST("medicines/analyze-symptoms")
    suspend fun analyzeSymptoms(
        @Header("Authorization") bearer: String,
        @Field("symptoms") symptoms: String,
        @Field("user_id") userId: String,
        @Field("family_id") familyId: Int? = null
    ): Response<com.example.halalyticscompose.data.model.SymptomsAnalysisResponse>

    @FormUrlEncoded
    @POST("medicines/search")
    suspend fun searchMedicine(
        @Header("Authorization") bearer: String,
        @Field("query") query: String,
        @Field("search_type") searchType: String = "name"
    ): Response<com.example.halalyticscompose.data.model.MedicineSearchResponse>

    @POST("medicines/safe-schedule")
    suspend fun generateSafeSchedule(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<com.example.halalyticscompose.data.model.SafeScheduleResponse>

    @FormUrlEncoded
    @POST("medicines/drug-food-conflict")
    suspend fun checkDrugFoodConflict(
        @Header("Authorization") bearer: String,
        @Field("medicine_name") medicineName: String? = null,
        @Field("medicine_id") medicineId: Int? = null,
        @Field("lookback_minutes") lookbackMinutes: Int = 180
    ): Response<com.example.halalyticscompose.data.model.DrugFoodConflictResponse>

    @FormUrlEncoded
    @POST("medicines/reminders")
    suspend fun createMedicineReminder(
        @Header("Authorization") bearer: String,
        @Field("id_user") userId: String,
        @Field("medicine_id") medicineId: Int,
        @Field("symptoms") symptoms: String? = null,
        @Field("frequency_per_day") frequencyPerDay: Int,
        @Field("start_date") startDate: String,
        @Field("end_date") endDate: String? = null,
        @Field("notes") notes: String? = null,
        @Field("family_id") familyId: Int? = null
    ): Response<com.example.halalyticscompose.data.model.MedicationReminderResponse>

    @GET("medicines/reminders")
    suspend fun getUserMedicineReminders(
        @Header("Authorization") bearer: String
    ): Response<com.example.halalyticscompose.data.model.UserRemindersResponse>

    @FormUrlEncoded
    @POST("medicines/reminders/log")
    suspend fun markMedicineAsTaken(
        @Header("Authorization") bearer: String,
        @Field("reminder_id") reminderId: Int,
        @Field("status") status: String
    ): Response<com.example.halalyticscompose.data.model.GenericResponse>

    @GET("medicines/reminders/next-dose")
    suspend fun getNextDose(
        @Header("Authorization") bearer: String
    ): Response<com.example.halalyticscompose.data.model.NextDoseResponse>

    // PHASE 5: ADMIN MONITOR
    @GET("admin/monitor/stats")
    suspend fun getAdminDashboardStats(
        @Header("Authorization") token: String
    ): Response<DashboardStatsResponse> 

    @GET("banners")
    suspend fun getBanners(): Response<BannerResponse>

    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("articles")
    suspend fun getHealthArticles(
        @Query("q") query: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("include_external") includeExternal: Boolean = true
    ): HealthArticleApiResponse

    @GET("articles/recommended")
    suspend fun getRecommendedArticles(
        @Header("Authorization") bearer: String,
        @Query("limit") limit: Int = 5
    ): HealthArticleApiResponse

    @GET("articles/{slug}")
    suspend fun getHealthArticleDetail(
        @Path("slug") slug: String
    ): HealthArticleDetailApiResponse

    @GET("admin/monitor/feed")
    suspend fun getAdminActivityFeed(
        @Header("Authorization") token: String
    ): Response<com.example.halalyticscompose.data.model.UserRemindersResponse> // Using similar model for feed list temporarily or create new one

    // ==================== OCR FEATURE (Real Implementation) ====================
    @Multipart
    @POST("ocr/submit")
    suspend fun submitOCR(
        @Header("Authorization") bearer: String,
        @Part frontImage: okhttp3.MultipartBody.Part,
        @Part backImage: okhttp3.MultipartBody.Part,
        @Part("ocr_text") ocrText: okhttp3.RequestBody? = null,
        @Part("family_member_id") familyMemberId: okhttp3.RequestBody? = null,
        @Part("language") language: okhttp3.RequestBody? = null
    ): ApiResponse<OCRProductData>

    @GET("ocr/history")
    suspend fun getUserOCRHistory(
        @Header("Authorization") bearer: String
    ): ApiResponse<List<OCRProductData>>

    @GET("ocr/statistics")
    suspend fun getOCRStatistics(
        @Header("Authorization") bearer: String
    ): ApiResponse<OCRStatisticsData>

    // ==================== FOOD RECOGNITION (Real Implementation) ====================
    @Multipart
    @POST("food/recognize-image")
    suspend fun recognizeFoodImage(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part
    ): ApiResponse<com.example.halalyticscompose.data.model.FoodRecognitionResponse>

    @GET("medicines/{id}")
    suspend fun getMedicineDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<com.example.halalyticscompose.data.model.MedicineSearchResponse>

    // ==================== ADVANCED AI HEALTH SUITE ====================
    
    // 1. Drug Interaction
    @FormUrlEncoded
    @POST("ai/interactions")
    suspend fun checkDrugInteraction(
        @Header("Authorization") bearer: String,
        @Field("drug_a_id") drugAId: Int? = null,
        @Field("drug_b_id") drugBId: Int? = null,
        @Field("drug_a_name") drugAName: String? = null,
        @Field("drug_b_name") drugBName: String? = null,
        @Field("family_id") familyId: Int? = null
    ): DrugInteractionResponse

    @GET("ai/drugs/search")
    suspend fun searchDrugs(
        @Header("Authorization") bearer: String,
        @Query("query") query: String
    ): MedicineSearchResponse // Reuse existing response model

    // 2. Pill Identification
    @Multipart
    @POST("ai/pill-identify")
    suspend fun identifyPill(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part,
        @Part("shape") shape: okhttp3.RequestBody? = null,
        @Part("color") color: okhttp3.RequestBody? = null,
        @Part("family_id") familyId: okhttp3.RequestBody? = null
    ): PillIdentifyResponse


    // 4. Medication Reminders (Enhanced)
    @POST("ai/reminders")
    suspend fun createAdvancedReminder(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, Any>
    ): MedicationReminderResponse

    @POST("ai/reminders/log")
    suspend fun logMedication(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, Any>
    ): GenericResponse

    @GET("ai/reminders")
    suspend fun getAdvancedReminders(
        @Header("Authorization") bearer: String
    ): MedicationReminderResponse // List variant in real impl
    @DELETE("medicines/reminders/{id}")
    suspend fun deleteAdvancedReminder(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): Response<GenericResponse>

    // 5. Halal Specialist & Alternatives
    @GET("ai/halal-alternatives")
    suspend fun getHalalAlternatives(
        @Header("Authorization") bearer: String,
        @Query("drug_id") drugId: Int,
        @Query("family_id") familyId: Int? = null
    ): com.example.halalyticscompose.data.model.HalalAlternativeResponse

    // 6. Health Journey (Metrics)
    @POST("health/metrics")
    suspend fun recordHealthMetric(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, Any>
    ): GenericResponse

    @GET("health/metrics/history")
    suspend fun getHealthMetricHistory(
        @Header("Authorization") bearer: String,
        @Query("metric_type") metricType: String
    ): HealthMetricResponse

    @GET("health/metrics/summary")
    suspend fun getHealthSummary(
        @Header("Authorization") bearer: String
    ): HealthMetricResponse

    @GET("health/diary")
    suspend fun getHealthDiary(
        @Header("Authorization") bearer: String,
        @Query("limit") limit: Int = 30
    ): HealthMetricResponse

    // ==================== BPOM VERIFICATION ====================
    @GET("bpom/search")
    suspend fun searchBpom(
        @Header("Authorization") bearer: String,
        @Query("q") query: String,
        @Query("family_id") familyId: Int? = null,
        @Query("include_ai") includeAi: Boolean? = null
    ): BpomSearchResponse

    @POST("bpom/check")
    @FormUrlEncoded
    suspend fun checkBpomRegistration(
        @Header("Authorization") bearer: String,
        @Field("code") code: String,
        @Field("family_id") familyId: Int? = null,
        @Field("include_ai") includeAi: Boolean? = null
    ): BpomCheckResponse

    @POST("bpom/analyze")
    @FormUrlEncoded
    suspend fun analyzeBpomProduct(
        @Header("Authorization") bearer: String,
        @Field("product_name") productName: String,
        @Field("ingredients_text") ingredientsText: String? = null,
        @Field("category") category: String? = null,
        @Field("barcode") barcode: String? = null,
        @Field("family_id") familyId: Int? = null
    ): BpomAnalyzeResponse

    // ==================== SKINCARE / KOSMETIK ====================
    @POST("skincare/analyze")
    @FormUrlEncoded
    suspend fun analyzeSkincareIngredients(
        @Header("Authorization") bearer: String,
        @Field("ingredients_text") ingredientsText: String? = null,
        @Field("image") imageBase64: String? = null,
        @Field("product_name") productName: String? = null,
        @Field("barcode") barcode: String? = null,
        @Field("family_id") familyId: Int? = null
    ): SkincareAnalysisResponse

    @POST("skincare/safety")
    @FormUrlEncoded
    suspend fun checkSkincareSafety(
        @Header("Authorization") bearer: String,
        @Field("ingredients_text") ingredientsText: String
    ): SafetyCheckResponse

    @POST("skincare/halal")
    @FormUrlEncoded
    suspend fun checkSkincareHalal(
        @Header("Authorization") bearer: String,
        @Field("ingredients_text") ingredientsText: String
    ): HalalCheckSkincareResponse

    @POST("ai/compare")
    suspend fun compareProducts(
        @Header("Authorization") bearer: String,
        @Body request: ComparisonRequest
    ): ComparisonResponse

    @POST("health/analyze")
    suspend fun analyzeHealth(
        @Header("Authorization") bearer: String,
        @Body profile: Map<String, Any?>
    ): ApiResponse<Map<String, Any>>

    @Multipart
    @POST("reports")
    suspend fun submitReport(
        @Header("Authorization") bearer: String,
        @Part("product_id") productId: okhttp3.RequestBody,
        @Part("reason") reason: okhttp3.RequestBody,
        @Part("laporan") laporan: okhttp3.RequestBody?,
        @Part evidenceImage: okhttp3.MultipartBody.Part?
    ): ReportResponse

    @POST("export-report")
    suspend fun exportMonthlyReport(
        @Header("Authorization") bearer: String,
        @Query("month") month: String? = null
    ): Response<com.example.halalyticscompose.data.model.ExportReportResponse>

    @GET("products/alternatives/{barcode}")
    suspend fun getProductAlternatives(
        @Path("barcode") barcode: String,
        @Header("Authorization") token: String? = null
    ): retrofit2.Response<com.example.halalyticscompose.data.model.ApiResponse<com.example.halalyticscompose.data.api.HalalAlternativeResponse>>

    // ==========================================
    // ADVANCED AI HEALTH SUITE EXPERIMENTAL
    // ==========================================


    @POST("nutrition-scans")
    suspend fun scanNutrition(@Body request: com.example.halalyticscompose.data.model.NutritionScanRequest): Response<com.example.halalyticscompose.data.model.NutritionScanResponse>

    @GET("medical-records")
    suspend fun getMedicalRecords(
        @Header("Authorization") bearer: String,
        @Query("user_id") userId: Int
    ): Response<com.example.halalyticscompose.data.model.MedicalRecordListResponse>

    @POST("medical-records")
    suspend fun addMedicalRecord(
        @Header("Authorization") bearer: String,
        @Body request: com.example.halalyticscompose.data.model.MedicalRecordRequest
    ): Response<com.example.halalyticscompose.data.model.MedicalRecordResponse>

    @POST("emergency/trigger")
    suspend fun triggerEmergency(
        @Header("Authorization") bearer: String,
        @Body request: com.example.halalyticscompose.data.model.EmergencyRequest
    ): Response<com.example.halalyticscompose.data.model.EmergencyResponse>

    // ==========================================================
    // 🤖 AI EXPANSION FEATURES (4-7)
    // ==========================================================

    // OFFLINE OCR (Feature 4)
    @GET("ocr/ingredients/sync")
    suspend fun syncIngredients(
        @Header("Authorization") bearer: String,
        @Query("updated_after") updatedAfter: String? = null
    ): Response<OcrSyncResponse>

    @POST("ocr/scan-result")
    suspend fun saveOcrResult(
        @Header("Authorization") bearer: String,
        @Body request: OcrScanResultRequest
    ): Response<GenericResponse>

    // SMART NUTRITION (Feature 5)
    @Multipart
    @POST("nutrition/log")
    suspend fun logMeal(
        @Header("Authorization") bearer: String,
        @Part image: okhttp3.MultipartBody.Part,
        @Part("meal_type") mealType: okhttp3.RequestBody
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<DailyNutritionLog>>

    @GET("nutrition/daily")
    suspend fun getDailyNutritionLog(
        @Header("Authorization") bearer: String,
        @Query("date") date: String? = null
    ): Response<NutritionDashboardResponse>

    @GET("nutrition/history")
    suspend fun getNutritionHistory(
        @Header("Authorization") bearer: String,
        @Query("days") days: Int = 30
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<List<NutritionHistoryItem>>>

    @POST("nutrition/goals")
    suspend fun setNutritionGoals(
        @Header("Authorization") bearer: String,
        @Body request: NutritionGoal
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<NutritionGoal>>

    @GET("nutrition/goals")
    suspend fun getNutritionGoals(
        @Header("Authorization") bearer: String
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<NutritionGoal>>

    // RECIPE AI (Feature 6)
    @GET("recipes")
    suspend fun getRecipes(
        @Header("Authorization") bearer: String,
        @Query("category") category: String? = null,
        @Query("halal_only") halalOnly: Boolean = false
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<List<Recipe>>>

    @GET("recipes/{id}")
    suspend fun getRecipeDetail(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<Recipe>>

    @GET("recipes/{id}/substitution")
    suspend fun getHalalSubstitution(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): Response<RecipeSubstitutionResponse>

    // HALOCODE (Expansion)
    @GET("halocode/experts")
    suspend fun getExperts(
        @Header("Authorization") bearer: String,
        @Query("specialization") specialization: String? = null,
        @Query("online_only") onlineOnly: Boolean = false
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<List<Expert>>>

    @POST("halocode/consultations")
    suspend fun startConsultation(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, Int> // {"expert_id": ...}
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<com.example.halalyticscompose.data.model.Consultation>>

    @GET("halocode/my-consultations")
    suspend fun getMyConsultations(
        @Header("Authorization") bearer: String
    ): Response<com.example.halalyticscompose.data.model.ApiResponse<List<com.example.halalyticscompose.data.model.Consultation>>>



    // AR FINDER endpoint removed



    // ==================== NOTIFICATION PREFERENCES ====================
    @GET("user/notification-preferences")
    suspend fun getNotificationPreferences(
        @Header("Authorization") bearer: String
    ): GenericResponse

    @FormUrlEncoded
    @PUT("user/notification-preferences")
    suspend fun updateNotificationPreferences(
        @Header("Authorization") bearer: String,
        @Field("medication_reminders") medicationReminders: Boolean? = null,
        @Field("promo_deals") promoDeals: Boolean? = null,
        @Field("weekly_report") weeklyReport: Boolean? = null,
        @Field("favorite_updates") favoriteUpdates: Boolean? = null,
        @Field("new_products") newProducts: Boolean? = null,
        @Field("security_alerts") securityAlerts: Boolean? = null
    ): GenericResponse

    // ==================== HALODOC EXPANSION ====================
    // Medical Profile
    @GET("medical-profile")
    suspend fun getMedicalProfile(@Header("Authorization") bearer: String): Response<GenericResponse>

    @POST("medical-profile")
    suspend fun updateMedicalProfile(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<GenericResponse>

    // Mental Health
    @GET("mental-health/topics")
    suspend fun getMentalHealthTopics(@Header("Authorization") bearer: String): Response<GenericResponse>

    @GET("mental-health/articles")
    suspend fun getMentalHealthArticles(@Header("Authorization") bearer: String): Response<GenericResponse>
    
    @GET("mental-health/experts")
    suspend fun getMentalHealthExperts(@Header("Authorization") bearer: String): Response<GenericResponse>

    @POST("mental-health/expert-request")
    suspend fun requestExpert(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<GenericResponse>

    // Help Center
    @GET("help/categories")
    suspend fun getHelpCategories(@Header("Authorization") bearer: String): Response<GenericResponse>

    @POST("help/request")
    suspend fun submitHelpRequest(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, @JvmSuppressWildcards String>
    ): Response<GenericResponse>

    // ==================== BLOOD DONATION ====================
    @GET("blood-events")
    suspend fun getBloodEvents(
        @Query("location") location: String? = null,
        @Query("page") page: Int = 1
    ): ApiResponse<List<BloodEvent>>

    @GET("blood-events/{id}")
    suspend fun getBloodEventDetail(
        @Path("id") id: String
    ): ApiResponse<BloodEvent>

    @GET("blood-stock")
    suspend fun getBloodStockSummary(): ApiResponse<List<BloodStock>>

    @GET("blood-emergency")
    suspend fun getActiveEmergencies(): ApiResponse<List<EmergencyBloodRequest>>

    @POST("blood-appointments")
    suspend fun createAppointment(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<DonorAppointment>

    @GET("blood-appointments/mine")
    suspend fun getMyAppointments(
        @Header("Authorization") bearer: String
    ): ApiResponse<List<DonorAppointment>>

    @GET("blood-appointments/{id}/qr")
    suspend fun getAppointmentQr(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<ApiResponse<Any>>

    @DELETE("blood-appointments/{id}")
    suspend fun cancelAppointment(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<ApiResponse<Any>>

    @GET("donor-card")
    suspend fun getDonorCard(
        @Header("Authorization") bearer: String
    ): ApiResponse<DonorCard>

    @POST("fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, String>
    ): Response<ApiResponse<Any>>
}
