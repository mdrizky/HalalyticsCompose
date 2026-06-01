package com.example.halalyticscompose.data.api

import com.example.halalyticscompose.BuildConfig
import com.example.halalyticscompose.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GroqApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"

    private suspend fun makeRequest(prompt: String, systemPrompt: String = ""): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_DEV_KEY") {
            println("Groq API Key is missing or invalid.")
            return@withContext null
        }

        val jsonObj = JSONObject().apply {
            put("model", "llama-3.3-70b-versatile")
            val messages = JSONArray()
            if (systemPrompt.isNotBlank()) {
                messages.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }
            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
            put("messages", messages)
            put("temperature", 0.3)
            put("response_format", JSONObject().put("type", "json_object"))
        }

        val body = jsonObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(GROQ_URL)
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val resBody = response.body?.string() ?: return@use null
                val root = JSONObject(resBody)
                val choices = root.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    return@use message?.optString("content")
                }
                return@use null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun analyzeHalal(productName: String, ingredientsText: String): HalalAnalysisResult? {
        val prompt = """
            Kamu adalah ahli halal food Indonesia. Analisis bahan-bahan produk berikut:
            Produk: $productName
            Bahan: $ingredientsText

            Jawab HANYA dengan format JSON object:
            {
              "status": "HALAL" atau "SYUBHAT" atau "HARAM",
              "alasan": "penjelasan singkat 1 kalimat",
              "bahan_masalah": ["list bahan bermasalah jika ada"]
            }
        """.trimIndent()

        val result = makeRequest(prompt) ?: return null
        return try {
            val json = JSONObject(result)
            val statusStr = json.optString("status", "UNKNOWN")
            val status = IntelligenceHalalStatus.fromString(statusStr)
            val alasan = json.optString("alasan", "Tidak ada alasan spesifik.")
            val bahanMasalahJson = json.optJSONArray("bahan_masalah")
            val bahanMasalah = mutableListOf<String>()
            if (bahanMasalahJson != null) {
                for (i in 0 until bahanMasalahJson.length()) {
                    bahanMasalah.add(bahanMasalahJson.getString(i))
                }
            }
            HalalAnalysisResult(status, alasan, bahanMasalah)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun analyzeIngredients(ingredientsText: String): List<IngredientDetail>? {
        val prompt = """
            Analisis bahan-bahan makanan berikut untuk konsumen awam Indonesia:
            $ingredientsText

            Untuk setiap bahan UTAMA (max 8), berikan JSON object dengan key "ingredients" berisi array:
            {
              "ingredients": [
                {
                  "name": "nama bahan",
                  "function": "fungsi dalam produk (1-3 kata)",
                  "risk": "risiko atau aman (1 kalimat)",
                  "level": "AMAN" atau "PERHATIAN" atau "HINDARI"
                }
              ]
            }
            PENTING: Harus valid JSON object dengan key "ingredients".
        """.trimIndent()

        val result = makeRequest(prompt) ?: return null
        return try {
            val json = JSONObject(result)
            val array = json.optJSONArray("ingredients") ?: return null
            val list = mutableListOf<IngredientDetail>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    IngredientDetail(
                        name = item.optString("name", "Unknown"),
                        function = item.optString("function", "-"),
                        risk = item.optString("risk", "-"),
                        level = item.optString("level", "AMAN")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAlternatives(productName: String, issues: String): List<AlternativeProduct>? {
        val prompt = """
            Produk: $productName
            Masalah/Kelemahan: $issues

            Berikan 3 rekomendasi alternatif produk sejenis yang lebih sehat dan halal dan mudah ditemukan di supermarket Indonesia.
            Format JSON object dengan key "alternatives" berisi array:
            {
              "alternatives": [
                {
                  "name": "nama/merek alternatif",
                  "reason": "kenapa lebih baik (1 kalimat)",
                  "tips": "tips tambahan singkat",
                  "halal": "HALAL"
                }
              ]
            }
            PENTING: Harus valid JSON object dengan key "alternatives".
        """.trimIndent()

        val result = makeRequest(prompt) ?: return null
        return try {
            val json = JSONObject(result)
            val array = json.optJSONArray("alternatives") ?: return null
            val list = mutableListOf<AlternativeProduct>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    AlternativeProduct(
                        name = item.optString("name", "Unknown"),
                        reason = item.optString("reason", "-"),
                        tips = item.optString("tips", "-"),
                        halal = item.optString("halal", "HALAL")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
