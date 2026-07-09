package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.ClashStats
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clash_stats")

@Singleton
class ClashStatsRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val STATS_KEY = stringPreferencesKey("clash_stats")

    fun getStats(): kotlinx.coroutines.flow.Flow<ClashStats> {
        return context.dataStore.data.map { prefs ->
            val str = prefs[STATS_KEY] ?: "{}"
            json.decodeFromString(str)
        }
    }

    suspend fun saveStats(stats: ClashStats) {
        context.dataStore.edit { prefs ->
            prefs[STATS_KEY] = json.encodeToString(stats)
        }
    }
}
