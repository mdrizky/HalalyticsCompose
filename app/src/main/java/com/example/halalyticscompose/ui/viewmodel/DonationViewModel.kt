package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DonationUiState(
    val loading: Boolean = false,
    val campaigns: List<DonationCampaignDto> = emptyList(),
    val selected: DonationCampaignDto? = null,
    val history: List<DonationHistoryItem> = emptyList(),
    val paymentUrl: String? = null,
    val snapToken: String? = null,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class DonationViewModel @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(DonationUiState())
    val state: StateFlow<DonationUiState> = _state.asStateFlow()

    fun loadCampaigns() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val token = session.getBearerToken() ?: return@launch
                val res = api.getDonationCampaigns(token)
                if (res.isSuccessful && res.body()?.success == true) {
                    _state.value = _state.value.copy(
                        loading = false,
                        campaigns = res.body()?.data ?: emptyList()
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Gagal memuat campaign donasi.")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Kesalahan jaringan")
            }
        }
    }

    fun selectCampaign(c: DonationCampaignDto) {
        _state.value = _state.value.copy(selected = c)
    }

    fun createDonation(amount: Double, anonymous: Boolean, message: String?) {
        val campaign = _state.value.selected ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val token = session.getBearerToken() ?: return@launch
                val res = api.createDonation(
                    token,
                    DonationCreateRequest(
                        campaignId = campaign.id,
                        amount = amount,
                        isAnonymous = anonymous,
                        donorMessage = message
                    )
                )
                if (res.isSuccessful && res.body()?.success == true) {
                    val data = res.body()?.data
                    _state.value = _state.value.copy(
                        loading = false,
                        paymentUrl = data?.paymentUrl,
                        snapToken = data?.snapToken
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Gagal membuat transaksi donasi.")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val token = session.getBearerToken() ?: return@launch
                val res = api.getDonationHistory(token)
                if (res.isSuccessful) {
                    _state.value = _state.value.copy(
                        loading = false,
                        history = res.body()?.data ?: emptyList()
                    )
                } else {
                    _state.value = _state.value.copy(loading = false)
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun clearPayment() {
        _state.value = _state.value.copy(paymentUrl = null, snapToken = null)
    }
}
