package com.example.halalyticscompose.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * SessionManager - Handles user session and preferences persistence
 */
class SessionManager(private val context: Context) {
    
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Throwable) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getContext(): Context = context
    
    companion object {
        private const val PREF_NAME = "halalytics_session"
        
        // Auth keys
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        
        // User keys
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_BLOOD_TYPE = "blood_type"
        private const val KEY_ALLERGY = "allergy"
        private const val KEY_MEDICAL_HISTORY = "medical_history"
        private const val KEY_ROLE = "role"
        private const val KEY_IMAGE_URL = "image_url"
        private const val KEY_EMERGENCY_CONTACT = "emergency_contact"
        private const val KEY_GENDER = "gender"
        
        // Health profile keys
        private const val KEY_AGE = "age"
        private const val KEY_HEIGHT = "height"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_BMI = "bmi"
        private const val KEY_ACTIVITY_LEVEL = "activity_level"
        private const val KEY_DIET_PREFERENCE = "diet_preference"
        private const val KEY_GOAL = "goal"
        
        // Stats keys
        private const val KEY_TOTAL_SCANS = "total_scans"
        private const val KEY_HALAL_COUNT = "halal_count"
        private const val KEY_SYUBHAT_COUNT = "syubhat_count"
        private const val KEY_STREAK = "streak"
        
        // Preferences keys
        private const val KEY_GLUTEN_FREE = "gluten_free"
        private const val KEY_NUT_ALLERGY = "nut_allergy"
        private const val KEY_STRICT_HALAL = "strict_halal"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_PRIVACY_MODE = "privacy_mode"
        private const val KEY_BIOMETRIC_LOCK = "biometric_lock"
        private const val KEY_AUTO_LOGOUT_ENABLED = "auto_logout_enabled"
        private const val KEY_AUTO_LOGOUT_MINUTES = "auto_logout_minutes"
        private const val KEY_LAST_BACKGROUND_TS = "last_background_ts"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        
        // Location keys
        private const val KEY_ADDRESS = "address"
        private const val KEY_CITY = "city"
        private const val KEY_PROVINCE = "province"
        
        @Volatile
        private var instance: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // ========================================
    // Authentication Methods
    // ========================================
    
    fun saveAuthToken(token: String, tokenType: String = "Bearer") {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    // ⚠️ NEW: Save complete session with role
    fun saveCompleteSession(
        token: String,
        userId: Int,
        username: String,
        fullName: String?,
        email: String,
        role: String,
        phone: String? = null,
        bloodType: String? = null,
        allergy: String? = null,
        medicalHistory: String? = null,
        imageUrl: String? = null
    ) {
        prefs.edit().apply {
            // Auth data
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_TOKEN_TYPE, "Bearer")
            putBoolean(KEY_IS_LOGGED_IN, true)
            
            // User data
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            putString(KEY_PHONE, phone)
            putString(KEY_BLOOD_TYPE, bloodType)
            putString(KEY_ALLERGY, allergy)
            putString(KEY_MEDICAL_HISTORY, medicalHistory)
            putString(KEY_IMAGE_URL, imageUrl)
            putString(KEY_EMERGENCY_CONTACT, phone)
            
            apply()
        }
    }
    
    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    
    fun getTokenType(): String = prefs.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
    
    fun getBearerToken(): String? {
        val token = getAuthToken()
        return if (token != null) "${getTokenType()} $token" else null
    }
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAuthToken() != null
    
    // ========================================
    // User Data Methods
    // ========================================
    
    fun saveUserData(
        userId: Int,
        username: String,
        fullName: String?,
        email: String,
        phone: String?,
        bloodType: String?,
        allergy: String?,
        medicalHistory: String?,
        role: String,
        imageUrl: String?,
        gender: String? = null
    ) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone)
            putString(KEY_BLOOD_TYPE, bloodType)
            putString(KEY_ALLERGY, allergy)
            putString(KEY_MEDICAL_HISTORY, medicalHistory)
            putString(KEY_ROLE, role)
            putString(KEY_IMAGE_URL, imageUrl)
            putString(KEY_GENDER, gender)
            putString(KEY_EMERGENCY_CONTACT, phone) // Use phone as default emergency contact for now
            apply()
        }
    }
    
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, 0)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getPhone(): String? = prefs.getString(KEY_PHONE, null)
    fun getBloodType(): String? = prefs.getString(KEY_BLOOD_TYPE, null)
    fun getAllergy(): String? = prefs.getString(KEY_ALLERGY, null)
    fun getMedicalHistory(): String? = prefs.getString(KEY_MEDICAL_HISTORY, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getImageUrl(): String? = prefs.getString(KEY_IMAGE_URL, null)
    fun getEmergencyContact(): String? = prefs.getString(KEY_EMERGENCY_CONTACT, null)
    fun getGender(): String? = prefs.getString(KEY_GENDER, null)
    
    fun saveRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun saveUser(name: String, email: String) {
        prefs.edit().apply {
            putString(KEY_FULL_NAME, name)
            putString(KEY_EMAIL, email)
            apply()
        }
    }
    
    // ========================================
    // Health Profile Methods
    // ========================================
    
    fun saveHealthProfile(
        age: Int?,
        height: Float?,
        weight: Float?,
        bmi: Float?,
        activityLevel: String?,
        dietPreference: String?,
        goal: String?,
        gender: String? = null
    ) {
        prefs.edit().apply {
            age?.let { putInt(KEY_AGE, it) }
            height?.let { putFloat(KEY_HEIGHT, it) }
            weight?.let { putFloat(KEY_WEIGHT, it) }
            bmi?.let { putFloat(KEY_BMI, it) }
            putString(KEY_ACTIVITY_LEVEL, activityLevel)
            putString(KEY_DIET_PREFERENCE, dietPreference)
            putString(KEY_GOAL, goal)
            putString(KEY_GENDER, gender)
            apply()
        }
    }
    
    fun getAge(): Int? = if (prefs.contains(KEY_AGE)) prefs.getInt(KEY_AGE, 0) else null
    fun getHeight(): Float? = if (prefs.contains(KEY_HEIGHT)) prefs.getFloat(KEY_HEIGHT, 0f) else null
    fun getWeight(): Float? = if (prefs.contains(KEY_WEIGHT)) prefs.getFloat(KEY_WEIGHT, 0f) else null
    fun getBmi(): Float? = if (prefs.contains(KEY_BMI)) prefs.getFloat(KEY_BMI, 0f) else null
    fun getActivityLevel(): String? = prefs.getString(KEY_ACTIVITY_LEVEL, null)
    fun getDietPreference(): String? = prefs.getString(KEY_DIET_PREFERENCE, null)
    fun getGoal(): String? = prefs.getString(KEY_GOAL, null)
    
    // ========================================
    // Location Methods
    // ========================================
    
    fun saveLocation(address: String?, city: String?, province: String?) {
        prefs.edit().apply {
            putString(KEY_ADDRESS, address)
            putString(KEY_CITY, city)
            putString(KEY_PROVINCE, province)
            apply()
        }
    }
    
    fun getAddress(): String? = prefs.getString(KEY_ADDRESS, null)
    fun getCity(): String? = prefs.getString(KEY_CITY, null)
    fun getProvince(): String? = prefs.getString(KEY_PROVINCE, null)
    
    // ========================================
    // Stats Methods
    // ========================================
    
    fun saveStats(totalScans: Int?, halalCount: Int?, syubhatCount: Int?, streak: Int?) {
        prefs.edit().apply {
            totalScans?.let { putInt(KEY_TOTAL_SCANS, it) }
            halalCount?.let { putInt(KEY_HALAL_COUNT, it) }
            syubhatCount?.let { putInt(KEY_SYUBHAT_COUNT, it) }
            streak?.let { putInt(KEY_STREAK, it) }
            apply()
        }
    }
    
    fun getTotalScans(): Int = prefs.getInt(KEY_TOTAL_SCANS, 0)
    fun getHalalCount(): Int = prefs.getInt(KEY_HALAL_COUNT, 0)
    fun getSyubhatCount(): Int = prefs.getInt(KEY_SYUBHAT_COUNT, 0)
    fun getStreak(): Int = prefs.getInt(KEY_STREAK, 0)
    
    // ========================================
    // Dietary Preferences Methods
    // ========================================
    
    fun saveDietaryPreferences(glutenFree: Boolean, nutAllergy: Boolean, strictHalal: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_GLUTEN_FREE, glutenFree)
            putBoolean(KEY_NUT_ALLERGY, nutAllergy)
            putBoolean(KEY_STRICT_HALAL, strictHalal)
            apply()
        }
    }
    
    fun savePreferences(darkMode: Boolean, notifEnabled: Boolean, language: String) {
        prefs.edit().apply {
            putBoolean(KEY_DARK_MODE, darkMode)
            putBoolean(KEY_NOTIF_ENABLED, notifEnabled)
            putString(KEY_LANGUAGE, language)
            apply()
        }
    }
    
    fun isGlutenFree(): Boolean = prefs.getBoolean(KEY_GLUTEN_FREE, false)
    fun hasNutAllergy(): Boolean = prefs.getBoolean(KEY_NUT_ALLERGY, false)
    fun isStrictHalal(): Boolean = prefs.getBoolean(KEY_STRICT_HALAL, true)
    
    // ========================================
    // App Settings Methods
    // ========================================
    
    fun saveDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }
    
    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)
    
    fun saveNotifEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply()
    }
    
    fun isNotifEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_ENABLED, true)

    fun saveLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "id") ?: "id"

    fun savePrivacyModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_MODE, enabled).apply()
    }

    fun isPrivacyModeEnabled(): Boolean = prefs.getBoolean(KEY_PRIVACY_MODE, true)

    fun saveBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
    }

    fun isBiometricLockEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)

    fun saveAutoLogoutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOGOUT_ENABLED, enabled).apply()
    }

    fun isAutoLogoutEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_LOGOUT_ENABLED, false)

    fun saveAutoLogoutMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_AUTO_LOGOUT_MINUTES, minutes).apply()
    }

    fun getAutoLogoutMinutes(): Int = prefs.getInt(KEY_AUTO_LOGOUT_MINUTES, 5)

    fun setLastBackgroundTimestamp(ts: Long) {
        prefs.edit().putLong(KEY_LAST_BACKGROUND_TS, ts).apply()
    }

    fun getLastBackgroundTimestamp(): Long = prefs.getLong(KEY_LAST_BACKGROUND_TS, 0L)

    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
    }
    
    // ========================================
    // Logout / Clear Session
    // ========================================
    
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
