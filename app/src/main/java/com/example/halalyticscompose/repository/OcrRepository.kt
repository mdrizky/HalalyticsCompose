package com.example.halalyticscompose.repository

import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.local.Dao.HaramIngredientDao
import com.example.halalyticscompose.data.local.Entities.HaramIngredientEntity
import com.example.halalyticscompose.data.model.OcrScanResultRequest
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepository @Inject constructor(
    private val api: ApiService,
    private val dao: HaramIngredientDao
) {
    val activeIngredients: Flow<List<HaramIngredientEntity>> = dao.getAllActive()

    suspend fun syncIngredients(token: String): Result<Unit> {
        return try {
            val lastUpdated = dao.getLastUpdated()
            val updatedAfter = lastUpdated?.let { Instant.ofEpochMilli(it).toString() }
            
            val response = api.syncIngredients("Bearer $token", updatedAfter)
            if (response.isSuccessful && response.body()?.success == true) {
                val ingredients = response.body()?.data?.map { 
                    HaramIngredientEntity(
                        id = it.id,
                        name = it.name,
                        aliases = it.aliases ?: emptyList(),
                        category = it.category,
                        severity = it.severity,
                        description = it.description,
                        isActive = it.isActive,
                        updatedAt = it.updatedAt?.let { timestamp -> 
                            runCatching { Instant.parse(timestamp).toEpochMilli() }.getOrNull() 
                        } ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                
                if (ingredients.isNotEmpty()) {
                    dao.insertAll(ingredients)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchLocal(query: String): List<HaramIngredientEntity> {
        return dao.searchIngredients("%$query%")
    }

    suspend fun saveResultToServer(token: String, request: OcrScanResultRequest): Result<Unit> {
        return try {
            val response = api.saveOcrResult("Bearer $token", request)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Save failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
