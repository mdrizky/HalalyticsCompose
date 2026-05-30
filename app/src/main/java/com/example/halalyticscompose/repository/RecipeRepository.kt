package com.example.halalyticscompose.repository

import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getRecipes(token: String, category: String? = null): Result<List<Recipe>> {
        return try {
            val response = api.getRecipes("Bearer $token", category)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Fetch recipes failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipeDetail(token: String, id: Int): Result<Recipe> {
        return try {
            val response = api.getRecipeDetail("Bearer $token", id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Fetch recipe detail failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHalalSubstitution(token: String, id: Int): Result<RecipeSubstitutionResponse> {
        return try {
            val response = api.getHalalSubstitution("Bearer $token", id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Substitution failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
