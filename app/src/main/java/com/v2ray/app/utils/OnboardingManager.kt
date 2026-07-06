package com.v2ray.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

object OnboardingManager {
    private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")

    fun isFirstLaunch(context: Context): Boolean {
        return runBlocking {
            val preferences = context.dataStore.data.first()
            preferences[FIRST_LAUNCH_KEY] ?: true
        }
    }

    suspend fun setFirstLaunchDone(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    fun reset(context: Context) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[FIRST_LAUNCH_KEY] = true
            }
        }
    }
}
