package com.example.halalyticscompose.di

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
        val original = chain.request()

        // Skip if already has Authorization header (e.g. manually set in login)
        if (original.header("Authorization") != null) {
            return chain.proceed(original)
        }

        val token = sessionManager.getAuthToken()
        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
