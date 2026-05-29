package com.example.halalyticscompose.repository

import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun logMeal(token: String, imageFile: File, mealType: String): Result<DailyNutritionLog> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val typeBody = mealType.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.logMealMultipart("Bearer $token", body, typeBody)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Log meal failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyLog(token: String, date: String? = null): Result<NutritionDashboardData> {
        return try {
            val response = api.getDailyNutritionLog("Bearer $token", date)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Fetch log failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(token: String, days: Int = 30): Result<List<NutritionHistoryItem>> {
        return try {
            val response = api.getNutritionHistory("Bearer $token", days)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Fetch history failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setGoals(token: String, goals: NutritionGoal): Result<Unit> {
        return try {
            val response = api.setNutritionGoals("Bearer $token", goals)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Set goals failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
