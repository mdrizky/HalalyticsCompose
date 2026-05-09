package com.example.halalyticscompose.repository

import com.example.halalyticscompose.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val api: ApiService,
) {
    // Daily mission logic removed to streamline core features.
}
