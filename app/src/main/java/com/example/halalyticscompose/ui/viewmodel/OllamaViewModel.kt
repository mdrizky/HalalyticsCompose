package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.OllamaApiService
import com.example.halalyticscompose.data.api.OllamaChatRequest
import com.example.halalyticscompose.data.api.OllamaMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OllamaViewModel @Inject constructor(
    private val apiService: OllamaApiService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _suggestions = MutableStateFlow<List<String>>(
        listOf("Apa itu Halalytics?", "Cek kehalalan bahan makanan", "Konsultasi gejala kesehatan ringan")
    )
    val suggestions: StateFlow<List<String>> = _suggestions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val systemPrompt = """
        Kamu adalah Asisten Kesehatan AI Halalytics yang cerdas dan empati. 
        Tugasmu:
        1. Menjawab pertanyaan seputar kesehatan, nutrisi, dan kehalalan produk.
        2. Jika informasi dari user kurang lengkap (misal: hanya bilang 'sakit perut'), tanyakan detail tambahan (seperti: 'sejak kapan?', 'di bagian mana?').
        3. Selalu berikan disclaimer bahwa ini bukan pengganti saran medis profesional.
        4. Di akhir setiap jawaban, berikan 2-3 pertanyaan lanjutan yang relevan untuk membantu user.
        Format pertanyaan lanjutan di baris terakhir diawali dengan 'Saran:' dipisahkan koma. 
        Contoh: 'Saran: Sejak kapan gejalanya muncul?, Apakah ada alergi obat?, Apa makanan terakhir yang dimakan?'
    """.trimIndent()

    fun sendMessage(content: String, model: String = "llama3") {
        if (content.isBlank()) return

        // Clear previous suggestions when user sends a message
        _suggestions.value = emptyList()

        val userMessage = ChatMessage(content, true)
        _messages.value = _messages.value + userMessage
        
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val ollamaMessages = mutableListOf(OllamaMessage("system", systemPrompt))
                ollamaMessages.addAll(_messages.value.map { 
                    OllamaMessage(if (it.isUser) "user" else "assistant", it.content)
                })

                val response = apiService.chat(
                    OllamaChatRequest(
                        model = model,
                        messages = ollamaMessages
                    )
                )

                if (response.isSuccessful) {
                    val assistantRawContent = response.body()?.message?.content ?: "Maaf, saya tidak mendapatkan respon."
                    
                    // Extract suggestions from response
                    val processedContent = processResponse(assistantRawContent)
                    
                    _messages.value = _messages.value + ChatMessage(processedContent, false)
                } else {
                    _error.value = "Terjadi kesalahan server AI (${response.code()})."
                    // Restore initial suggestions on error so user can try something else
                    _suggestions.value = listOf("Apa itu Halalytics?", "Cek kehalalan bahan makanan")
                }
            } catch (e: Exception) {
                _error.value = "Gagal terhubung ke Ollama: ${e.message}. Pastikan layanan AI lokal Anda aktif."
                _suggestions.value = listOf("Cara menjalankan AI lokal", "Bantuan aplikasi")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun processResponse(raw: String): String {
        val lines = raw.lines()
        val suggestionLine = lines.find { it.startsWith("Saran:", ignoreCase = true) }
        
        if (suggestionLine != null) {
            val suggestionsText = suggestionLine.substringAfter("Saran:").trim()
            val extracted = suggestionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            _suggestions.value = extracted
            
            // Return content without the suggestion line
            return raw.replace(suggestionLine, "").trim()
        }
        
        // Fallback: if no formal "Saran:", try to find questions
        val questions = raw.lines().filter { it.contains("?") }.takeLast(2).map { it.trim().removePrefix("- ").removePrefix("* ") }
        if (questions.size >= 2) {
            _suggestions.value = questions
        }

        return raw
    }

    fun clearChat() {
        _messages.value = emptyList()
        _suggestions.value = listOf("Apa itu Halalytics?", "Cek kehalalan bahan makanan", "Konsultasi gejala kesehatan ringan")
    }
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
