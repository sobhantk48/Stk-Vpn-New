package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.AdBlockRule
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "adblock")

@Singleton
class AdBlockRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val RULES_KEY = stringPreferencesKey("adblock_rules")
    private val ENABLED_KEY = stringPreferencesKey("adblock_enabled")

    suspend fun getRules(): List<AdBlockRule> {
        return context.dataStore.data.map { prefs ->
            val str = prefs[RULES_KEY] ?: "[]"
            json.decodeFromString(str)
        }.firstOrNull() ?: emptyList()
    }

    suspend fun saveRules(rules: List<AdBlockRule>) {
        context.dataStore.edit { prefs ->
            prefs[RULES_KEY] = json.encodeToString(rules)
        }
    }

    suspend fun addRule(rule: AdBlockRule) {
        val current = getRules()
        saveRules(current + rule)
    }

    suspend fun removeRule(id: String) {
        val current = getRules()
        saveRules(current.filter { it.id != id })
    }

    suspend fun isEnabled(): Boolean {
        return context.dataStore.data.map { prefs ->
            prefs[ENABLED_KEY]?.toBoolean() ?: false
        }.firstOrNull() ?: false
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ENABLED_KEY] = enabled.toString()
        }
    }
}
