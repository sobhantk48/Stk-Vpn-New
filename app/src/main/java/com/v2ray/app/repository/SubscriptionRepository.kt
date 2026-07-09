package com.v2ray.app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.v2ray.app.model.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subscriptions")

@Singleton
class SubscriptionRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val SUBSCRIPTIONS_KEY = stringPreferencesKey("subscriptions")

    fun getAllSubscriptions(): Flow<List<Subscription>> {
        return context.dataStore.data.map { preferences ->
            val jsonStr = preferences[SUBSCRIPTIONS_KEY] ?: "[]"
            json.decodeFromString(jsonStr)
        }
    }

    suspend fun saveSubscriptions(subscriptions: List<Subscription>) {
        context.dataStore.edit { preferences ->
            preferences[SUBSCRIPTIONS_KEY] = json.encodeToString(subscriptions)
        }
    }

    suspend fun addSubscription(subscription: Subscription) {
        val current = getAllSubscriptions().firstOrNull() ?: emptyList()
        saveSubscriptions(current + subscription)
    }

    suspend fun removeSubscription(id: String) {
        val current = getAllSubscriptions().firstOrNull() ?: emptyList()
        saveSubscriptions(current.filter { it.id != id })
    }

    suspend fun updateSubscription(subscription: Subscription) {
        val current = getAllSubscriptions().firstOrNull() ?: emptyList()
        saveSubscriptions(current.map { if (it.id == subscription.id) subscription else it })
    }
}
