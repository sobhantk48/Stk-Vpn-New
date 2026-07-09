package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.data.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "profiles")

@Singleton
class ProfileRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val PROFILES_KEY = stringPreferencesKey("profiles")
    private val SELECTED_ID_KEY = stringPreferencesKey("selected_profile_id")

    fun getAllProfiles(): Flow<List<Profile>> {
        return context.dataStore.data.map { preferences ->
            val jsonStr = preferences[PROFILES_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }
    }

    suspend fun insertProfile(profile: Profile) {
        val current = getAllProfiles().firstOrNull() ?: emptyList()
        val updated = if (current.any { it.id == profile.id }) {
            current.map { if (it.id == profile.id) profile else it }
        } else {
            current + profile
        }
        context.dataStore.edit { preferences ->
            preferences[PROFILES_KEY] = json.encodeToString(updated)
        }
    }

    suspend fun deleteProfile(profile: Profile) {
        val current = getAllProfiles().firstOrNull() ?: emptyList()
        val updated = current.filter { it.id != profile.id }
        context.dataStore.edit { preferences ->
            preferences[PROFILES_KEY] = json.encodeToString(updated)
        }
    }

    suspend fun setSelected(profileId: String) {
        val current = getAllProfiles().firstOrNull() ?: emptyList()
        val updated = current.map { 
            it.copy(selected = it.id == profileId)
        }
        context.dataStore.edit { preferences ->
            preferences[PROFILES_KEY] = json.encodeToString(updated)
            preferences[SELECTED_ID_KEY] = profileId
        }
    }

    suspend fun updateCustomSni(profileId: String, sni: String) {
        val current = getAllProfiles().firstOrNull() ?: emptyList()
        val updated = current.map { 
            if (it.id == profileId) it.copy(customSni = sni) else it
        }
        context.dataStore.edit { preferences ->
            preferences[PROFILES_KEY] = json.encodeToString(updated)
        }
    }

    suspend fun updateFrontingDomain(profileId: String, frontingDomain: String) {
        val current = getAllProfiles().firstOrNull() ?: emptyList()
        val updated = current.map { 
            if (it.id == profileId) it.copy(frontingDomain = frontingDomain) else it
        }
        context.dataStore.edit { preferences ->
            preferences[PROFILES_KEY] = json.encodeToString(updated)
        }
    }

    suspend fun updateRealitySettings(
        profileId: String,
        publicKey: String,
        shortId: String,
        serverName: String,
        fingerprint: String
    ) {
        val current = getAllProfiles().firstOrNull() ?: emptyList()
        val updated = current.map { 
            if (it.id == profileId) it.copy(
                realityPublicKey = publicKey,
                realityShortId = shortId,
                realityServerName = serverName,
                realityFingerprint = fingerprint
            ) else it
        }
        context.dataStore.edit { preferences ->
            preferences[PROFILES_KEY] = json.encodeToString(updated)
        }
    }
}
