package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.BrokenConfigReport
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "broken_configs")

@Singleton
class BrokenConfigRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val REPORTS_KEY = stringPreferencesKey("broken_config_reports")

    suspend fun getAllReports(): List<BrokenConfigReport> {
        return context.dataStore.data.map { prefs ->
            val str = prefs[REPORTS_KEY] ?: "[]"
            json.decodeFromString(str)
        }.firstOrNull() ?: emptyList()
    }

    suspend fun saveReports(reports: List<BrokenConfigReport>) {
        context.dataStore.edit { prefs ->
            prefs[REPORTS_KEY] = json.encodeToString(reports)
        }
    }

    suspend fun addReport(report: BrokenConfigReport) {
        val current = getAllReports()
        saveReports(current + report)
    }

    suspend fun clearReports() {
        saveReports(emptyList())
    }
}
