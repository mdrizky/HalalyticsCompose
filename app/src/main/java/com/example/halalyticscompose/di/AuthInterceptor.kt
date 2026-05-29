package com.example.halalyticscompose.di

import android.util.Log
import com.example.halalyticscompose.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * AuthInterceptor — automatically attaches Bearer token to every API request.
 * This removes the need for each ViewModel to manually prepend "Bearer ".
 * Requests that already have an Authorization header are left untouched.
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.getAuthToken()
        
        // Log outgoing request for debugging
        Log.d("HALALYTICS_API", "Request: ${originalRequest.method} ${originalRequest.url}")

        // Check for token expiry locally before sending
        if (!token.isNullOrEmpty() && sessionManager.isTokenExpired()) {
            Log.w("HALALYTICS_API", "Token expired locally. Forcing logout.")
            sessionManager.logout()
            // We can return the request as-is (it will fail with 401 anyway)
        }

        // Skip if already has Authorization header (e.g. manually set in login)
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val request = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(request)
        
        // Handle 401 Unauthenticated globally
        if (response.code == 401) {
            sessionManager.logout()
            // In a real app, you might want to use a Broadcaster or a shared flow to navigate to login
            // For now, we clear the session so next app open goes to login
        }
        
        return response
    }
}
