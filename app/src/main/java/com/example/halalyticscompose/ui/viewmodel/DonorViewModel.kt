package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DonorViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _bloodEvents = MutableStateFlow<List<BloodEvent>>(emptyList())
    val bloodEvents: StateFlow<List<BloodEvent>> = _bloodEvents

    private val _bloodStock = MutableStateFlow<List<BloodStock>>(emptyList())
    val bloodStock: StateFlow<List<BloodStock>> = _bloodStock

    private val _donorCard = MutableStateFlow<DonorCard?>(null)
    val donorCard: StateFlow<DonorCard?> = _donorCard

    private val _donorHistory = MutableStateFlow<List<DonorAppointment>>(emptyList())
    val donorHistory: StateFlow<List<DonorAppointment>> = _donorHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _activeEmergencies = MutableStateFlow<List<EmergencyBloodRequest>>(emptyList())
    val activeEmergencies: StateFlow<List<EmergencyBloodRequest>> = _activeEmergencies

    fun loadDonorDashboard(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
                
                // Parallel fetching with supervisorScope so one failure doesn't crash the others
                kotlinx.coroutines.supervisorScope {
                    launch { 
                        try { _bloodEvents.value = apiService.getBloodEvents().data ?: emptyList() }
                        catch (e: Exception) { android.util.Log.e("DonorViewModel", "Error fetching events", e) }
                    }
                    launch { 
                        try { _bloodStock.value = apiService.getBloodStockSummary().data ?: emptyList() }
                        catch (e: Exception) { android.util.Log.e("DonorViewModel", "Error fetching stock", e) }
                    }
                    launch { 
                        try { _donorCard.value = apiService.getDonorCard(bearer).data }
                        catch (e: Exception) { android.util.Log.e("DonorViewModel", "Error fetching donor card", e) }
                    }
                    launch { 
                        try { _donorHistory.value = apiService.getMyAppointments(bearer).data ?: emptyList() }
                        catch (e: Exception) { android.util.Log.e("DonorViewModel", "Error fetching appointments", e) }
                    }
                    launch { 
                        try { _activeEmergencies.value = apiService.getActiveEmergencies().data ?: emptyList() }
                        catch (e: Exception) { android.util.Log.e("DonorViewModel", "Error fetching emergencies", e) }
                    }
                }
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Gagal memuat data donor"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerForEvent(token: String, eventId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
                apiService.createAppointment(bearer, mapOf("event_id" to eventId))
                onSuccess()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Gagal mendaftar event"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun registerForEventWithPayload(token: String, payload: Map<String, Any>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
                apiService.createAppointment(bearer, payload)
                onSuccess()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Gagal mendaftar event"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleVoluntaryStatus(isActive: Boolean, token: String) {
        viewModelScope.launch {
            try {
                val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"
                // Assuming an endpoint exists, let's call it and update local state
                val request = mapOf("is_voluntary_donor" to isActive)
                apiService.updateVoluntaryStatus(bearer, request)
                
                // Update local state
                _donorCard.value = _donorCard.value?.copy(isVoluntaryDonor = isActive)
            } catch (e: Exception) {
                _error.value = "Gagal memperbarui status: ${e.message}"
            }
        }
    }
}
