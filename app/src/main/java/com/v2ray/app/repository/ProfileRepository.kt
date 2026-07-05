package com.v2ray.app.repository

import android.content.Context
import android.content.SharedPreferences
import com.v2ray.app.data.Profile
import com.v2ray.app.utils.Logger
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
        Logger.writeLog("ProfileRepository initialized with SharedPreferences")
        loadFromStorage()
    }

    private fun loadFromStorage() {
        try {
            val raw = prefs.getString("profiles", null)
            if (!raw.isNullOrBlank()) {
                val list = json.decodeFromString<List<Profile>>(raw)
                _profiles.value = list
                Logger.writeLog("Loaded ${list.size} profiles from SharedPreferences")
                // اگر هیچ پروفایلی selected نیست و لیست خالی نیست، اولین را انتخاب کن
                if (list.isNotEmpty() && list.none { it.selected }) {
                    val first = list.first()
                    select(first.id)
                }
            } else {
                Logger.writeLog("No profiles found in SharedPreferences")
            }
        } catch (e: Exception) {
            Logger.writeError("Load profiles failed", e)
            // در صورت خطا، لیست را خالی کن
            _profiles.value = emptyList()
        }
    }

    private fun saveToStorage() {
        try {
            val raw = json.encodeToString(_profiles.value)
            prefs.edit().putString("profiles", raw).apply()
            Logger.writeLog("Saved ${_profiles.value.size} profiles to SharedPreferences")
        } catch (e: Exception) {
            Logger.writeError("Save profiles failed", e)
        }
    }

    private fun save() = CoroutineScope(Dispatchers.IO).launch { saveToStorage() }

    fun add(profile: Profile) {
        _profiles.update { it + profile }
        save()
        // اگر اولین پروفایل است، آن را انتخاب کن
        if (_profiles.value.size == 1) {
            select(profile.id)
        }
        Logger.writeLog("Added: ${profile.name}")
    }

    fun update(profile: Profile) {
        _profiles.update { list ->
            list.map { if (it.id == profile.id) profile else it }
        }
        save()
    }

    fun delete(id: String) {
        _profiles.update { it.filterNot { it.id == id } }
        save()
        // اگر پروفایل حذف شده selected بود، اولین پروفایل را انتخاب کن
        val currentList = _profiles.value
        if (currentList.isNotEmpty() && currentList.none { it.selected }) {
            select(currentList.first().id)
        }
        Logger.writeLog("Deleted: $id")
    }

    fun select(id: String) {
        _profiles.update { list ->
            list.map { it.copy(selected = it.id == id) }
        }
        save()
        Logger.writeLog("Selected profile: $id")
    }

    fun getSelected(): Profile? = _profiles.value.firstOrNull { it.selected }

    fun clear() {
        _profiles.value = emptyList()
        save()
    }
}
