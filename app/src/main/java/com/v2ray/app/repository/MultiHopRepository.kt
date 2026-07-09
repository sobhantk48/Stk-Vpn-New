package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.MultiHopConfig
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "multi_hop")

@Singleton
class MultiHopRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val KEY = stringPreferencesKey("multi_hop_configs")

    suspend fun getAll(): List<MultiHopConfig> {
        return context.dataStore.data.map { prefs ->
            val str = prefs[KEY] ?: "[]"
            json.decodeFromString(str)
        }.firstOrNull() ?: emptyList()
    }

    suspend fun save(configs: List<MultiHopConfig>) {
        context.dataStore.edit { prefs ->
            prefs[KEY] = json.encodeToString(configs)
        }
    }

    suspend fun add(config: MultiHopConfig) {
        val current = getAll()
        save(current + config)
    }

    suspend fun remove(id: String) {
        val current = getAll()
        save(current.filter { it.id != id })
    }

    suspend fun update(config: MultiHopConfig) {
        val current = getAll()
        save(current.map { if (it.id == config.id) config else it })
    }

    suspend fun getEnabled(): MultiHopConfig? {
        return getAll().firstOrNull { it.enabled }
    }
}
