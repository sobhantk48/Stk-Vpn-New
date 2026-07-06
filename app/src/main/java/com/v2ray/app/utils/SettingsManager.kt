package com.v2ray.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsManager {
    private val KILL_SWITCH_KEY = booleanPreferencesKey("kill_switch")
    private val SPLIT_ENABLED_KEY = booleanPreferencesKey("split_enabled")

    suspend fun saveKillSwitch(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KILL_SWITCH_KEY] = enabled
        }
    }

    suspend fun getKillSwitch(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[KILL_SWITCH_KEY] ?: false
    }

    suspend fun saveSplitEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SPLIT_ENABLED_KEY] = enabled
        }
    }

    suspend fun getSplitEnabled(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[SPLIT_ENABLED_KEY] ?: false
    }
}
