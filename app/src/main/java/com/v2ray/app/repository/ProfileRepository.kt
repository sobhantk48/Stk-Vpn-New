package com.v2ray.app.repository

import android.content.Context
import android.content.SharedPreferences
import com.v2ray.app.data.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

object ProfileRepository {

    private lateinit var prefs: SharedPreferences
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles

    fun initialize(ctx: Context) {
        prefs = ctx.getSharedPreferences("v2ray_profiles", Context.MODE_PRIVATE)
        loadFromStorage()
    }

    private fun loadFromStorage() {
        try {
            val raw = prefs.getString("profiles", null)
            if (!raw.isNullOrBlank()) {
                val list = json.decodeFromString<List<Profile>>(raw)
                _profiles.value = list


                if (list.isNotEmpty() && list.none { it.selected }) {
                    select(list.first().id)
                }
            } else {
            }
        } catch (e: Exception) {
            _profiles.value = emptyList()
        }
    }

    private fun saveToStorage() {
        try {
            val raw = json.encodeToString(_profiles.value)
            prefs.edit().putString("profiles", raw).apply()
        } catch (e: Exception) {
        }
    }

    private fun save() = CoroutineScope(Dispatchers.IO).launch { saveToStorage() }

    fun add(profile: Profile) {
        _profiles.update { it + profile }
        save()

        if (_profiles.value.size == 1) {
            select(profile.id)
        }

    }

    fun update(profile: Profile) {
        _profiles.update { list ->
            list.map { if (it.id == profile.id) profile else it }
        }
        save()
    }

    fun delete(id: String) {
        val wasSelected = _profiles.value.firstOrNull { it.id == id }?.selected == true

        _profiles.update { it.filterNot { it.id == id } }
        save()

        if (wasSelected) {
            val list = _profiles.value
            if (list.isNotEmpty()) {
                select(list.first().id)
            }
        }

    }

    fun select(id: String) {
        _profiles.update { list ->
            list.map { it.copy(selected = it.id == id) }
        }
        save()
    }

    fun getSelected(): Profile? = _profiles.value.firstOrNull { it.selected }

    fun clear() {
        _profiles.value = emptyList()
        save()
    }
}
