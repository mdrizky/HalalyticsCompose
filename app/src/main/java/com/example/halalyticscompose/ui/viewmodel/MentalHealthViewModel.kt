package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.halalyticscompose.data.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.example.halalyticscompose.utils.SessionManager

@HiltViewModel
class MentalHealthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _topics = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val topics: StateFlow<List<Map<String, Any>>> = _topics.asStateFlow()

    private val _articles = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val articles: StateFlow<List<Map<String, Any>>> = _articles.asStateFlow()

    private val _experts = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val experts: StateFlow<List<Map<String, Any>>> = _experts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _news = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val news: StateFlow<List<Map<String, Any>>> = _news.asStateFlow()

    fun loadNews() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.getMentalHealthArticles("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _news.value = response.body()?.data as? List<Map<String, Any>> ?: emptyList()
                    _articles.value = _news.value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadData() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val topicsResp = apiService.getMentalHealthTopics("Bearer $token")
                if (topicsResp.isSuccessful && topicsResp.body()?.success == true) {
                    _topics.value = topicsResp.body()?.data as? List<Map<String, Any>> ?: emptyList()
                }

                val articlesResp = apiService.getMentalHealthArticles("Bearer $token")
                if (articlesResp.isSuccessful && articlesResp.body()?.success == true) {
                    _articles.value = articlesResp.body()?.data as? List<Map<String, Any>> ?: emptyList()
                }

                val expertsResp = apiService.getMentalHealthExperts("Bearer $token")
                if (expertsResp.isSuccessful && expertsResp.body()?.success == true) {
                    _experts.value = expertsResp.body()?.data as? List<Map<String, Any>> ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
