package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id_user")
    val idUser: Int,
    
    @SerializedName("username")
    val username: String = "",
    
    @SerializedName("full_name")
    val fullName: String? = "",
    
    @SerializedName("email")
    val email: String = "",
    
    @SerializedName("google_id")
    val googleId: String? = null,
    
    @SerializedName("facebook_id")
    val facebookId: String? = null,
    
    @SerializedName("social_provider")
    val socialProvider: String? = null,
    
    @SerializedName("onboarding_level")
    val onboardingLevel: String = "Newcomer",
    
    @SerializedName("login_streak")
    val loginStreak: Int = 0,
    
    @SerializedName("total_articles_read")
    val totalArticlesRead: Int = 0,
    
    @SerializedName("total_categories_explored")
    val totalCategoriesExplored: Int = 0,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerializedName("birth_date")
    val birthDate: String? = null,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("current_streak")
    val currentStreak: Int = 0,
    
    @SerializedName("longest_streak")
    val longestStreak: Int = 0,
    
    @SerializedName("phone")
    val phone: String? = "",
    
    @SerializedName("bio")
    val bio: String? = "",
    
    @SerializedName("allergies")
    val allergies: String? = "",
    
    @SerializedName("allergy")
    val allergy: String? = "",
    
    @SerializedName("medical_history")
    val medicalHistory: String? = "",
    
    @SerializedName("blood_type")
    val bloodType: String? = "-",
    
    @SerializedName("has_diabetes")
    val hasDiabetes: Boolean = false,
    
    @SerializedName("total_scans")
    val totalScans: Int = 0,
    
    @SerializedName("halal_products_count")
    val halalProductsCount: Int = 0,
    
    @SerializedName("role")
    val role: String = "user",
    
    @SerializedName("active")
    val active: Boolean = true,
    
    @SerializedName("image")
    val image: String? = null,
    
    @SerializedName("goal")
    val goal: String? = "",
    
    @SerializedName("diet_preference")
    val dietPreference: String? = "None",
    
    @SerializedName("activity_level")
    val activityLevel: String? = "medium",
    
    @SerializedName("address")
    val address: String? = "",
    
    @SerializedName("language")
    val language: String = "id",
    
    @SerializedName("age")
    val age: Int? = 0,
    
    @SerializedName("height")
    val height: Double? = 0.0,
    
    @SerializedName("weight")
    val weight: Double? = 0.0,
    
    @SerializedName("bmi")
    val bmi: Double? = 0.0,
    
    @SerializedName("notif_enabled")
    val notifEnabled: Boolean = true,
    
    @SerializedName("dark_mode")
    val darkMode: Boolean = false,
    
    @SerializedName("emergency_contact")
    val emergencyContact: String? = "",
    
    @SerializedName("total_donor_points")
    val totalDonorPoints: Int = 0
)
