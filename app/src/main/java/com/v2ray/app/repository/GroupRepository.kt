package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "groups")

@Singleton
class GroupRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val GROUPS_KEY = stringPreferencesKey("groups")

    fun getAllGroups(): Flow<List<Group>> {
        return context.dataStore.data.map { preferences ->
            val jsonStr = preferences[GROUPS_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }
    }

    suspend fun saveGroups(groups: List<Group>) {
        context.dataStore.edit { preferences ->
            preferences[GROUPS_KEY] = json.encodeToString(groups)
        }
    }

    suspend fun addGroup(group: Group) {
        val current = getAllGroups().firstOrNull() ?: emptyList()
        saveGroups(current + group)
    }

    suspend fun removeGroup(id: String) {
        val current = getAllGroups().firstOrNull() ?: emptyList()
        saveGroups(current.filter { it.id != id })
    }

    suspend fun updateGroup(group: Group) {
        val current = getAllGroups().firstOrNull() ?: emptyList()
        saveGroups(current.map { if (it.id == group.id) group else it })
    }
}
