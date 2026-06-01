package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.utils.RoleHelper
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val role: String) : AuthState()
    data class Unauthenticated(val isExpired: Boolean = false) : AuthState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            // Memberikan waktu untuk animasi splash screen
            delay(1500)
            
            try {
                val token = sessionManager.getAuthToken()
                val isExpired = sessionManager.isTokenExpired()
                val role = RoleHelper.normalizeRole(sessionManager.getRole())

                if (token.isNullOrBlank()) {
                    _authState.value = AuthState.Unauthenticated(isExpired = false)
                } else if (isExpired) {
                    try { sessionManager.logout() } catch (_: Exception) {}
                    _authState.value = AuthState.Unauthenticated(isExpired = true)
                } else {
                    _authState.value = AuthState.Authenticated(role = role)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated(isExpired = false)
            }
        }
    }
}
