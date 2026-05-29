package com.example.halalyticscompose.navigation

object Routes {
    // Auth Routes
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE_SETUP = "profile_setup"

    // User Routes
    const val USER_HOME = "user_home"
    const val SCAN = "scan"
    const val SCAN_RESULT = "scan_result/{barcode}"
    const val MANUAL_INPUT = "manual_input"
    const val AI_ANALYSIS = "ai_analysis/{productId}"
    const val AI_CHAT = "ai_chat"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "history_detail/{historyId}"
    const val ARTICLE_LIST = "article_list"
    const val ARTICLE_DETAIL = "article_detail/{articleId}"
    const val DONATION_LIST = "donation_list"
    const val DONATION_DETAIL = "donation_detail/{campaignId}"
    const val DONATION_FORM = "donation_form/{campaignId}"
    const val DONATION_PAYMENT = "donation_payment/{orderId}"
    const val DONATION_SUCCESS = "donation_success/{orderId}"

    // Blood Donor Routes
    const val DONOR_HOME = "donor_home"
    const val DONOR_EVENTS = "donor_events"
    const val DONOR_HISTORY = "donor_history"
    const val DONOR_EVENT_DETAIL = "donor_event_detail/{eventId}"
    const val EMERGENCY_REQUEST = "emergency_request"
    const val DONOR_FORM = "donor_form/{eventId}"

    // User Profile & Others
    const val PROFILE = "profile"
    const val HEALTH_PROFILE = "health_profile"
    const val CONSULTATION = "consultation"
    const val CONSULTATION_CHAT = "consultation_chat/{sessionId}"
    const val PRODUCT_DETAIL = "product_detail/{barcode}"
    const val SETTINGS = "settings"
    const val PRIVACY_POLICY = "privacy_policy"

    // Nutritionist Routes
    const val NUTRITIONIST_HOME = "nutritionist_home"
    const val PATIENT_LIST = "patient_list"
    const val PATIENT_DETAIL = "patient_detail/{userId}"
    const val MEAL_PLAN = "meal_plan/{userId}"
    const val AI_REVIEW = "ai_review"
    const val ARTICLE_EDITOR = "article_editor"
    const val NUTRITIONIST_PROFILE = "nutritionist_profile"
    const val NUTRITIONIST_SCHEDULE = "nutritionist_schedule"

    // Admin Routes
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USER_MANAGEMENT = "admin_user_management"
    const val ADMIN_AI_PROMPT = "admin_ai_prompt"
    const val ADMIN_HALAL_RULES = "admin_halal_rules"
    const val ADMIN_PRODUCT_MODERATION = "admin_product_moderation"
    const val ADMIN_BPOM = "admin_bpom"
    const val ADMIN_DONATION_MANAGEMENT = "admin_donation_management"
    const val ADMIN_AI_LOGS = "admin_ai_logs"
    const val ADMIN_ARTICLES = "admin_articles"
    const val ADMIN_SETTINGS = "admin_settings"

    // Helper untuk route dengan parameter
    fun scanResult(barcode: String) = "scan_result/$barcode"
    fun aiAnalysis(productId: String) = "ai_analysis/$productId"
    fun articleDetail(articleId: String) = "article_detail/$articleId"
    fun donationDetail(campaignId: String) = "donation_detail/$campaignId"
    fun donationForm(campaignId: String) = "donation_form/$campaignId"
    fun patientDetail(userId: String) = "patient_detail/$userId"
    fun mealPlan(userId: String) = "meal_plan/$userId"
    fun historyDetail(historyId: String) = "history_detail/$historyId"
    fun productDetail(barcode: String) = "product_detail/$barcode"
    fun consultationChat(sessionId: String) = "consultation_chat/$sessionId"
}
