package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.services.FirebaseRealtimeListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _history = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    val history: StateFlow<List<ScanHistoryItem>> = _history.asStateFlow()

    private val _stats = MutableStateFlow<ScanStats?>(null)
    val stats: StateFlow<ScanStats?> = _stats.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var realtimeJob: Job? = null

    fun loadHistory(token: String, userId: Int) {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getRealtimeScanHistory("Bearer $token")
                if (response.success) {
                    _history.value = response.data?.data ?: emptyList()
                    _stats.value = response.stats
                } else {
                    _history.value = emptyList()
                    _errorMessage.value = response.message ?: "Riwayat gagal dimuat dari server."
                }

                try {
                    val listener = FirebaseRealtimeListener(userId)
                    listener.listenToScanHistory().collect { update ->
                        val currentList = _history.value
                        if (currentList.none { it.id == update.id }) {
                            val newItem = ScanHistoryItem(
                                id = update.id,
                                productName = update.product_name,
                                productImage = null,
                                barcode = update.barcode,
                                halalStatus = update.halal_status,
                                source = "realtime",
                                scanMethod = "unknown",
                                createdAt = "Baru saja"
                            )
                            _history.value = listOf(newItem) + currentList
                        }
                    }
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    Log.e("ScanHistoryViewModel", "Realtime listener error", e)
                }
            } catch (e: Exception) {
                Log.e("ScanHistoryViewModel", "Failed to load history", e)
                _history.value = emptyList()
                _errorMessage.value = "Gagal memuat riwayat: ${e.message ?: "Unknown error"}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteHistory(token: String, historyId: Int) {
        viewModelScope.launch {
            val snapshot = _history.value
            val optimistic = snapshot.filter { it.id != historyId }
            _history.value = optimistic
            try {
                val response = apiService.deleteScanHistory("Bearer $token", historyId)
                if (!response.isSuccessful) {
                    _history.value = snapshot
                    _errorMessage.value = "Gagal hapus di server (${response.code()})."
                }
            } catch (e: Exception) {
                _history.value = snapshot
                _errorMessage.value = "Gagal hapus riwayat: ${e.message ?: "Unknown error"}"
                Log.e("ScanHistoryViewModel", "Failed to delete history", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
    }
}
