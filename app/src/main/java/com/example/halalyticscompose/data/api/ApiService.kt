package com.example.halalyticscompose.data.api

import com.example.halalyticscompose.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @FormUrlEncoded
    @POST("auth/google")
    suspend fun googleLogin(@Field("id_token") idToken: String): Response<LoginResponse>

    @FormUrlEncoded
    @POST("auth/facebook")
    suspend fun facebookLogin(@Field("access_token") accessToken: String): Response<LoginResponse>

    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") bearer: String): Response<ProfileResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("user/logout")
    suspend fun logout(@Header("Authorization") bearer: String): GenericResponse

    @FormUrlEncoded
    @POST("forgot-password")
    suspend fun forgotPassword(@Field("email") email: String): GenericResponse

    @FormUrlEncoded
    @POST("user/change-password")
    suspend fun changePassword(
        @Header("Authorization") bearer: String,
        @Field("current_password") current: String,
        @Field("new_password") new: String,
        @Field("new_password_confirmation") confirm: String
    ): GenericResponse

    @FormUrlEncoded
    @POST("user/profile/update")
    suspend fun updateProfile(
        @Header("Authorization") bearer: String,
        @Field("full_name") fullName: String? = null,
        @Field("email") email: String? = null,
        @Field("phone") phone: String? = null,
        @Field("blood_type") bloodType: String? = null,
        @Field("allergy") allergy: String? = null,
        @Field("medical_history") medicalHistory: String? = null,
        @Field("age") age: Int? = null,
        @Field("height") height: Double? = null,
        @Field("weight") weight: Double? = null,
        @Field("diet_preference") dietPreference: String? = null,
        @Field("language") language: String? = null,
        @Field("goal") goal: String? = null,
        @Field("activity_level") activityLevel: String? = null,
        @Field("gender") gender: String? = null,
        @Field("bio") bio: String? = null,
        @Field("address") address: String? = null,
        @Field("emergency_contact") emergencyContact: String? = null,
        @Field("birth_date") birthDate: String? = null
    ): GenericResponse

    @Multipart
    @POST("user/profile/update")
    suspend fun updateProfileMultipart(
        @Header("Authorization") bearer: String,
        @Part image: MultipartBody.Part,
        @Part("full_name") fullName: RequestBody? = null,
        @Part("email") email: RequestBody? = null,
        @Part("phone") phone: RequestBody? = null,
        @Part("blood_type") bloodType: RequestBody? = null,
        @Part("allergy") allergy: RequestBody? = null,
        @Part("medical_history") medicalHistory: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part("height") height: RequestBody? = null,
        @Part("weight") weight: RequestBody? = null,
        @Part("diet_preference") dietPreference: RequestBody? = null,
        @Part("language") language: RequestBody? = null,
        @Part("goal") goal: RequestBody? = null,
        @Part("activity_level") activityLevel: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null,
        @Part("bio") bio: RequestBody? = null,
        @Part("address") address: RequestBody? = null,
        @Part("emergency_contact") emergencyContact: RequestBody? = null,
        @Part("birth_date") birthDate: RequestBody? = null
    ): GenericResponse

    @POST("user/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") bearer: String,
        @Body body: Map<String, String>
    ): GenericResponse

    @FormUrlEncoded
    @POST("auth/sync")
    suspend fun syncUser(
        @Field("firebase_uid") firebaseUid: String,
        @Field("email") email: String,
        @Field("display_name") displayName: String? = null,
        @Field("fcm_token") fcmToken: String? = null
    ): GenericResponse

    // ==================== ADMIN ====================
    @GET("admin/dashboard/stats")
    suspend fun getDashboardStats(@Header("Authorization") bearer: String): DashboardStatsResponse

    @GET("admin/products/pending")
    suspend fun getPendingProducts(@Header("Authorization") bearer: String): PendingProductsResponse

    @PUT("admin/products/{id}/approve")
    suspend fun approveProduct(@Header("Authorization") bearer: String, @Path("id") id: Int): ApprovalResponse

    @PUT("admin/products/{id}/reject")
    suspend fun rejectProduct(@Header("Authorization") bearer: String, @Path("id") id: Int, @Body reason: Map<String, String>): ApprovalResponse

    @GET("admin/users")
    suspend fun getAllUsers(@Header("Authorization") bearer: String): UserListResponse

    @PUT("admin/users/{id}")
    suspend fun updateUserByAdmin(@Header("Authorization") bearer: String, @Path("id") id: Int, @Body data: Map<String, @JvmSuppressWildcards Any?>): GenericResponse

    // ==================== SCAN & PRODUCTS ====================
    @GET("products/scan/{barcode}")
    suspend fun scanUnified(@Header("Authorization") bearer: String, @Path("barcode") barcode: String): Response<UnifiedScanResponse>

    @GET("products/search/{barcode}")
    suspend fun searchByBarcode(@Header("Authorization") bearer: String, @Path("barcode") barcode: String): Response<ApiResponse<ProductInfo>>

    @GET("products/{barcode}")
    suspend fun getProduct(@Path("barcode") barcode: String): Response<ApiResponse<ProductDetailData>>

    @GET("https://world.openfoodfacts.org/api/v0/product/{barcode}.json")
    suspend fun getOpenFoodFactsProduct(@Path("barcode") barcode: String): Response<OpenFoodFactsResponse>

    @POST("halal/check")
    suspend fun checkHalal(@Body request: HalalCheckRequest): Response<HalalCheckResponse>

    @GET("products/{barcode}/alternatives")
    suspend fun getProductAlternatives(@Path("barcode") barcode: String, @Header("Authorization") bearer: String): Response<ApiResponse<HalalAlternativeResponse>>

    @POST("scan-history")
    suspend fun recordScan(@Header("Authorization") bearer: String, @Body request: RecordScanRequest): GenericResponse

    @GET("scan-history")
    suspend fun getRealtimeScanHistory(@Header("Authorization") bearer: String): RealtimeScanHistoryResponse

    @GET("scan-history/{id}")
    suspend fun getScanHistoryDetail(@Header("Authorization") bearer: String, @Path("id") id: Int): ScanHistoryDetailResponse

    @DELETE("scan-history/{id}")
    suspend fun deleteScanHistory(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<GenericResponse>

    // ==================== FAVORITES ====================
    @POST("favorites")
    suspend fun addFavorite(@Header("Authorization") bearer: String, @Body request: AddFavoriteRequest): Response<ApiResponse<FavoriteItem>>

    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<GenericResponse>

    @GET("favorites")
    suspend fun getFavorites(@Header("Authorization") bearer: String): FavoriteResponse

    // ==================== NOTIFICATIONS ====================
    @GET("notifications")
    suspend fun getNotifications(@Header("Authorization") bearer: String): NotificationResponse

    @POST("notifications/{id}/read")
    suspend fun markAsRead(@Header("Authorization") bearer: String, @Path("id") id: Int): GenericResponse

    @POST("notifications/read-all")
    suspend fun markAllAsRead(@Header("Authorization") bearer: String): GenericResponse

    // ==================== DONATION ====================
    @GET("donation/campaigns")
    suspend fun getDonationCampaigns(@Header("Authorization") bearer: String): Response<DonationCampaignsResponse>

    @POST("donation/create")
    suspend fun createDonation(@Header("Authorization") bearer: String, @Body request: DonationCreateRequest): Response<DonationCreateResponse>

    @GET("donation/history")
    suspend fun getDonationHistory(@Header("Authorization") bearer: String): Response<DonationHistoryResponse>

    // ==================== DONOR / BLOOD ====================
    @GET("donor/events")
    suspend fun getBloodEvents(): ApiResponse<List<BloodEvent>>

    @GET("donor/blood-stock")
    suspend fun getBloodStockSummary(): ApiResponse<List<BloodStock>>

    @GET("donor/card")
    suspend fun getDonorCard(@Header("Authorization") bearer: String): ApiResponse<DonorCard>

    @GET("donor/appointments")
    suspend fun getMyAppointments(@Header("Authorization") bearer: String): ApiResponse<List<DonorAppointment>>

    @GET("donor/emergencies")
    suspend fun getActiveEmergencies(): ApiResponse<List<EmergencyBloodRequest>>

    @POST("donor/appointments")
    suspend fun createAppointment(@Header("Authorization") bearer: String, @Body payload: Map<String, @JvmSuppressWildcards Any>): GenericResponse

    @POST("donor/voluntary-status")
    suspend fun updateVoluntaryStatus(@Header("Authorization") bearer: String, @Body request: Map<String, @JvmSuppressWildcards Any>): GenericResponse

    // ==================== EMERGENCY P3K ====================
    @POST("emergency/trigger")
    suspend fun triggerEmergency(@Header("Authorization") bearer: String, @Body request: EmergencyRequest): EmergencyResponse

    // ==================== FAMILY ====================
    @GET("family/profiles")
    suspend fun getFamilyProfiles(@Header("Authorization") bearer: String): ApiResponse<List<FamilyProfile>>

    @POST("family/profiles")
    suspend fun addFamilyProfile(@Header("Authorization") bearer: String, @Body profile: Map<String, @JvmSuppressWildcards Any?>): Response<GenericResponse>

    @Multipart
    @POST("family/profiles")
    suspend fun addFamilyProfileMultipart(
        @Header("Authorization") bearer: String,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("relation") relation: RequestBody,
        @Part("birth_date") birthDate: RequestBody? = null,
        @Part("blood_type") bloodType: RequestBody? = null,
        @Part("allergy") allergy: RequestBody? = null,
        @Part("medical_history") medicalHistory: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null
    ): Response<GenericResponse>

    @PUT("family/profiles/{id}")
    suspend fun updateFamilyProfile(@Header("Authorization") bearer: String, @Path("id") id: Int, @Body profile: Map<String, @JvmSuppressWildcards Any?>): Response<GenericResponse>

    @Multipart
    @POST("family/profiles/{id}")
    suspend fun updateFamilyProfileMultipart(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody? = null,
        @Part("relation") relation: RequestBody? = null,
        @Part("birth_date") birthDate: RequestBody? = null,
        @Part("blood_type") bloodType: RequestBody? = null,
        @Part("allergy") allergy: RequestBody? = null,
        @Part("medical_history") medicalHistory: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null
    ): Response<GenericResponse>

    @DELETE("family/profiles/{id}")
    suspend fun deleteFamilyProfile(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<GenericResponse>

    // ==================== MEDICINE ====================
    @GET("user/medicine-reminders")
    suspend fun getUserMedicineReminders(@Header("Authorization") bearer: String): Response<ApiResponse<List<MedicationReminderItem>>>

    @FormUrlEncoded
    @POST("user/medicine-reminders/{id}/taken")
    suspend fun markMedicineAsTaken(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int,
        @Field("status") status: String = "taken"
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("medicines/analyze-symptoms")
    suspend fun analyzeSymptoms(
        @Header("Authorization") bearer: String,
        @Field("symptoms") symptoms: String,
        @Field("user_id") userId: String,
        @Field("family_id") familyId: Int? = null
    ): Response<SymptomsAnalysisResponse>

    @GET("medicines/search")
    suspend fun searchMedicine(
        @Header("Authorization") bearer: String,
        @Query("query") query: String,
        @Query("search_type") searchType: String = "name"
    ): Response<MedicineSearchResponse>

    @FormUrlEncoded
    @POST("medicines/reminders")
    suspend fun createMedicineReminder(
        @Header("Authorization") bearer: String,
        @Field("user_id") userId: String,
        @Field("medicine_id") medicineId: Int,
        @Field("symptoms") symptoms: String? = null,
        @Field("frequency_per_day") frequencyPerDay: Int,
        @Field("start_date") startDate: String,
        @Field("end_date") endDate: String? = null,
        @Field("notes") notes: String? = null,
        @Field("family_id") familyId: Int? = null
    ): Response<MedicationReminderResponse>

    @GET("medicines/next-dose")
    suspend fun getNextDose(@Header("Authorization") bearer: String): Response<NextDoseResponse>

    @DELETE("medicines/reminders/{id}")
    suspend fun deleteAdvancedReminder(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<GenericResponse>

    @GET("medicines/{id}")
    suspend fun getMedicineDetail(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): Response<MedicineDetailResponse>

    @POST("medicines/safe-schedule")
    suspend fun generateSafeSchedule(@Header("Authorization") bearer: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<SafeScheduleResponse>

    @GET("medicines/personal-risk")
    suspend fun getPersonalRiskScore(@Header("Authorization") bearer: String, @Query("date") date: String? = null): PersonalRiskScoreResponse

    @FormUrlEncoded
    @POST("medicines/drug-food-conflict")
    suspend fun checkDrugFoodConflict(
        @Header("Authorization") bearer: String,
        @Field("medicine_name") medicineName: String? = null,
        @Field("medicine_id") medicineId: Int? = null,
        @Field("lookback_minutes") lookbackMinutes: Int = 180
    ): Response<DrugFoodConflictResponse>

    // ==================== HEALTH AI ====================
    @FormUrlEncoded
    @POST("ai/interactions")
    suspend fun checkDrugInteraction(
        @Header("Authorization") bearer: String,
        @Field("drug_a_id") drugAId: Int? = null,
        @Field("drug_b_id") drugBId: Int? = null,
        @Field("drug_a_name") drugAName: String? = null,
        @Field("drug_b_name") drugBName: String? = null,
        @Field("family_id") familyId: Int? = null
    ): Response<DrugInteractionResponse>

    @Multipart
    @POST("health/pill-identify")
    suspend fun identifyPill(
        @Header("Authorization") bearer: String,
        @Part image: MultipartBody.Part,
        @Part("shape") shape: RequestBody? = null,
        @Part("color") color: RequestBody? = null,
        @Part("family_id") familyId: RequestBody? = null
    ): PillIdentifyResponse

    @POST("health/reminders/advanced")
    suspend fun createAdvancedReminder(@Header("Authorization") bearer: String, @Body request: Map<String, @JvmSuppressWildcards Any?>): Response<GenericResponse>

    @GET("health/halal-alternatives/{drugId}")
    suspend fun getHalalAlternatives(@Header("Authorization") bearer: String, @Path("drugId") drugId: Int): ApiResponse<HalalAlternativeData>

    @POST("health/metrics")
    suspend fun recordHealthMetric(@Header("Authorization") bearer: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<GenericResponse>

    @GET("health/metrics/history")
    suspend fun getHealthMetricHistory(@Header("Authorization") bearer: String, @Query("type") type: String): HealthMetricResponse

    // ==================== NUTRITION & HEALTH ====================
    @POST("user/nutrition/log")
    suspend fun logMeal(@Header("Authorization") bearer: String, @Body request: MealLogRequest): Response<ApiResponse<DailyNutritionLog>>

    @Multipart
    @POST("user/nutrition/log")
    suspend fun logMealMultipart(
        @Header("Authorization") bearer: String,
        @Part image: MultipartBody.Part,
        @Part("meal_type") mealType: RequestBody
    ): Response<ApiResponse<DailyNutritionLog>>

    @GET("user/nutrition/daily")
    suspend fun getDailyNutritionLog(@Header("Authorization") bearer: String, @Query("date") date: String? = null): Response<ApiResponse<NutritionDashboardData>>

    @GET("user/nutrition/history")
    suspend fun getNutritionHistory(@Header("Authorization") bearer: String, @Query("days") days: Int = 30): Response<ApiResponse<List<NutritionHistoryItem>>>

    @POST("user/nutrition/goals")
    suspend fun setNutritionGoals(@Header("Authorization") bearer: String, @Body request: NutritionGoal): Response<GenericResponse>

    @POST("ai/meal-analysis")
    suspend fun analyzeMeal(@Header("Authorization") token: String, @Body request: MealAnalysisRequest): Response<ApiResponse<MealData>>

    // ==================== RECIPE ====================
    @GET("recipes")
    suspend fun getRecipes(
        @Header("Authorization") bearer: String,
        @Query("category") category: String? = null,
        @Query("halal_only") halalOnly: Boolean? = null
    ): Response<ApiResponse<List<Recipe>>>

    @GET("recipes/{id}")
    suspend fun getRecipeDetail(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<ApiResponse<Recipe>>

    @GET("recipes/{id}/substitution")
    suspend fun getHalalSubstitution(@Header("Authorization") bearer: String, @Path("id") id: Int): Response<RecipeSubstitutionResponse>

    // ==================== COMPARE ====================
    @POST("products/compare")
    suspend fun compareProducts(@Header("Authorization") bearer: String, @Body request: ComparisonRequest): ComparisonResponse

    // ==================== AI & OCR ====================
    @POST("ai/chat")
    suspend fun sendAiChat(@Header("Authorization") bearer: String, @Body request: AiChatRequest): Response<AiChatResponse>

    @POST("ai/analyze-ingredients")
    suspend fun analyzeIngredients(@Header("Authorization") bearer: String, @Body request: AiAnalysisRequest): AiAnalysisResponse

    @POST("ocr/sync")
    suspend fun syncIngredients(@Header("Authorization") bearer: String, @Query("updated_after") updatedAfter: String? = null): Response<OcrSyncResponse>

    @POST("ocr/save")
    suspend fun saveOcrResult(@Header("Authorization") bearer: String, @Body request: OcrScanResultRequest): Response<GenericResponse>

    @Multipart
    @POST("ocr/submit")
    suspend fun submitOCR(
        @Header("Authorization") bearer: String,
        @Part frontImage: MultipartBody.Part,
        @Part backImage: MultipartBody.Part,
        @Part("ocr_text") ocrText: RequestBody? = null,
        @Part("family_member_id") familyMemberId: RequestBody? = null,
        @Part("language") language: RequestBody? = null
    ): ApiResponse<OCRProductData>

    @GET("ocr/history")
    suspend fun getUserOCRHistory(@Header("Authorization") bearer: String): ApiResponse<List<OCRProductData>>

    @GET("ocr/statistics")
    suspend fun getOCRStatistics(@Header("Authorization") bearer: String): ApiResponse<OCRStatisticsData>

    // ==================== AI REPORT ====================
    @GET("ai/weekly-report")
    suspend fun getWeeklyReport(@Header("Authorization") bearer: String, @Query("days") days: Int = 7): AiReportResponse

    @GET("ai/medical-reports")
    suspend fun getMedicalReportsHistory(@Header("Authorization") bearer: String): ApiResponse<List<Map<String, Any>>>

    // ==================== FOOD SCAN ====================
    @GET("food/search")
    suspend fun searchFood(@Header("Authorization") bearer: String, @Query("q") query: String): ApiResponse<List<StreetFood>>

    @GET("food/popular")
    suspend fun getPopularFoods(@Header("Authorization") bearer: String): ApiResponse<List<StreetFood>>

    @POST("food/analyze")
    suspend fun analyzeFood(@Header("Authorization") token: String, @Body request: FoodAnalysisRequest): ApiResponse<FoodAnalysis>

    @Multipart
    @POST("food/recognize")
    suspend fun recognizeFoodImage(@Header("Authorization") token: String, @Part image: MultipartBody.Part): ApiResponse<FoodRecognitionResponse>

    // ==================== NUTRITION SCANNER ====================
    @POST("nutrition/scan")
    suspend fun scanNutrition(@Body request: NutritionScanRequest): Response<NutritionScanResponse>

    // ==================== HEALTH DATA ====================
    @GET("health/intake/daily")
    suspend fun getDailyIntake(@Header("Authorization") bearer: String): Response<DailyIntakeResponse>

    @GET("health/insight/daily")
    suspend fun getAiDailyInsight(@Header("Authorization") bearer: String): Response<AiInsightResponse>

    @GET("health/score")
    suspend fun getHealthScore(@Header("Authorization") bearer: String): Response<HealthScoreResponse>

    @GET("health/diary")
    suspend fun getHealthDiary(@Header("Authorization") bearer: String, @Query("limit") limit: Int = 30): ApiResponse<List<HealthMetricData>>

    @POST("health/analyze")
    suspend fun analyzeHealth(
        @Header("Authorization") bearer: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<Map<String, Any>>

    // ==================== HEALTH ENCYCLOPEDIA ====================
    @GET("health/encyclopedia")
    suspend fun getHealthEncyclopedia(@Query("type") type: String? = null, @Query("search") search: String? = null): Response<HealthEncyclopediaResponse>

    @GET("health/encyclopedia/{id}")
    suspend fun getHealthEncyclopediaById(@Path("id") id: Int): Response<HealthEncyclopediaDetailResponse>

    // ==================== HEALTH ARTICLES ====================
    @GET("articles")
    suspend fun getHealthArticles(
        @Query("q") query: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("include_external") includeExternal: Boolean? = null
    ): ApiResponse<List<HealthArticleItem>>

    @GET("articles/{idOrSlug}")
    suspend fun getHealthArticleDetail(@Path("idOrSlug") idOrSlug: String): ApiResponse<HealthArticleItem>

    @GET("articles/recommended")
    suspend fun getRecommendedArticles(
        @Header("Authorization") bearer: String,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<HealthArticleItem>>

    // ==================== BPOM ====================
    @GET("bpom/search")
    suspend fun searchBpom(
        @Header("Authorization") bearer: String,
        @Query("query") query: String,
        @Query("family_id") familyId: Int? = null,
        @Query("include_ai") includeAi: Boolean = false
    ): BpomSearchResponse

    @GET("bpom/check")
    suspend fun checkBpomRegistration(
        @Header("Authorization") bearer: String,
        @Query("code") code: String,
        @Query("family_id") familyId: Int? = null,
        @Query("include_ai") includeAi: Boolean = false
    ): BpomCheckResponse

    @FormUrlEncoded
    @POST("bpom/analyze")
    suspend fun analyzeBpomProduct(
        @Header("Authorization") bearer: String,
        @Field("product_name") productName: String,
        @Field("ingredients_text") ingredientsText: String? = null,
        @Field("category") category: String? = null,
        @Field("barcode") barcode: String? = null,
        @Field("family_id") familyId: Int? = null
    ): BpomAnalyzeResponse

    // ==================== SKINCARE ====================
    @FormUrlEncoded
    @POST("skincare/analyze")
    suspend fun analyzeSkincareIngredients(
        @Header("Authorization") bearer: String,
        @Field("ingredients_text") ingredientsText: String? = null,
        @Field("image_base64") imageBase64: String? = null,
        @Field("product_name") productName: String? = null,
        @Field("barcode") barcode: String? = null,
        @Field("family_id") familyId: Int? = null
    ): SkincareAnalysisResponse

    @FormUrlEncoded
    @POST("skincare/safety-check")
    suspend fun checkSkincareSafety(
        @Header("Authorization") bearer: String,
        @Field("ingredients_text") ingredientsText: String
    ): SafetyCheckResponse

    @FormUrlEncoded
    @POST("skincare/halal-check")
    suspend fun checkSkincareHalal(
        @Header("Authorization") bearer: String,
        @Field("ingredients_text") ingredientsText: String
    ): HalalCheckSkincareResponse

    // ==================== MENTAL HEALTH ====================
    @GET("mental-health/articles")
    suspend fun getMentalHealthArticles(@Header("Authorization") bearer: String): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("mental-health/topics")
    suspend fun getMentalHealthTopics(@Header("Authorization") bearer: String): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("mental-health/experts")
    suspend fun getMentalHealthExperts(@Header("Authorization") bearer: String): Response<ApiResponse<List<Map<String, Any>>>>

    // ==================== CONTRIBUTION ====================
    @Multipart
    @POST("contributions/submit")
    suspend fun submitContribution(
        @Header("Authorization") bearer: String,
        @Part("product_name") productName: RequestBody,
        @Part("barcode") barcode: RequestBody? = null,
        @Part("complaint") complaint: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): ContributionResponse

    // ==================== HELP CENTER ====================
    @GET("help/categories")
    suspend fun getHelpCategories(@Header("Authorization") bearer: String): Response<ApiResponse<List<Map<String, Any>>>>

    @POST("help/request")
    suspend fun submitHelpRequest(@Header("Authorization") bearer: String, @Body requestData: Map<String, String>): Response<ApiResponse<Map<String, Any>>>

    // ==================== MEDICAL ====================
    @GET("user/medical-profile")
    suspend fun getMedicalProfile(@Header("Authorization") bearer: String): Response<ApiResponse<Map<String, Any>>>

    @POST("user/medical-profile")
    suspend fun updateMedicalProfile(@Header("Authorization") bearer: String, @Body data: Map<String, @JvmSuppressWildcards Any?>): Response<GenericResponse>

    @GET("medical/records")
    suspend fun getMedicalRecords(@Header("Authorization") bearer: String, @Query("user_id") userId: Int? = null): Response<MedicalRecordListResponse>

    @POST("medical/records")
    suspend fun addMedicalRecord(@Header("Authorization") bearer: String, @Body request: MedicalRecordRequest): Response<MedicalRecordResponse>

    // ==================== NUTRITIONIST ====================
    @GET("nutritionist/dashboard")
    suspend fun getNutritionistDashboard(@Header("Authorization") bearer: String): NutritionistDashboardResponse

    // ==================== PRODUCT REQUEST ====================
    @Multipart
    @POST("products/request-verification")
    suspend fun uploadProductRequest(
        @Header("Authorization") bearer: String,
        @Part imageFront: MultipartBody.Part,
        @Part imageBack: MultipartBody.Part,
        @Part("barcode") barcode: RequestBody,
        @Part("product_name") productName: RequestBody,
        @Part("ocr_text") ocrText: RequestBody? = null
    ): Response<GenericResponse>

    // ==================== REPORT ====================
    @Multipart
    @POST("reports")
    suspend fun submitReport(
        @Header("Authorization") bearer: String,
        @Part("product_id") productId: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("laporan") laporan: RequestBody? = null,
        @Part evidenceImage: MultipartBody.Part? = null
    ): ReportResponse

    // ==================== BMI ====================
    @POST("ai/bmi-advice")
    suspend fun getBmiAdvice(@Header("Authorization") bearer: String, @Body request: BmiAdviceRequest): Response<BmiAdviceResponse>

    // ==================== BANNERS & MISC ====================
    @GET("banners")
    suspend fun getBanners(): Response<BannerResponse>

    @GET("products/recommendations")
    suspend fun getRecommendations(@Header("Authorization") bearer: String, @Query("category") category: String): RecommendationsResponse

    @GET("user/stats")
    suspend fun getUserStats(@Header("Authorization") bearer: String): UserStatsResponse

    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("https://api.unsplash.com/search/photos")
    suspend fun searchUnsplashPhotos(@Query("query") query: String, @Header("Authorization") clientId: String, @Query("per_page") perPage: Int = 5): Response<UnsplashSearchResponse>
}
