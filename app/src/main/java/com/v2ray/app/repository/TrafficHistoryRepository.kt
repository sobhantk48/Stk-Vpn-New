package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.TrafficHistory
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "traffic_history")

@Singleton
class TrafficHistoryRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val HISTORY_KEY = stringPreferencesKey("traffic_history")

    suspend fun getAllHistory(): List<TrafficHistory> {
        return context.dataStore.data.map { prefs ->
            val str = prefs[HISTORY_KEY] ?: "[]"
            json.decodeFromString(str)
        }.firstOrNull() ?: emptyList()
    }

    suspend fun saveHistory(history: List<TrafficHistory>) {
        context.dataStore.edit { prefs ->
            prefs[HISTORY_KEY] = json.encodeToString(history)
        }
    }

    suspend fun addEntry(entry: TrafficHistory) {
        val current = getAllHistory()
        val updated = (current + entry).takeLast(10000)
        saveHistory(updated)
    }

    suspend fun getHistoryByDate(date: Long): List<TrafficHistory> {
        val all = getAllHistory()
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        val targetDay = cal.get(Calendar.DAY_OF_YEAR)
        val targetYear = cal.get(Calendar.YEAR)
        return all.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.date }
            c.get(Calendar.DAY_OF_YEAR) == targetDay && c.get(Calendar.YEAR) == targetYear
        }
    }

    suspend fun clearHistory() {
        saveHistory(emptyList())
    }
}
