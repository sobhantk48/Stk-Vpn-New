package com.v2ray.app.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.v2ray.app.data.ConnectionState
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.repository.ProfileRepository
import com.v2ray.app.service.V2RayService
import com.v2ray.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ConnectionState())
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _current = MutableStateFlow<Profile?>(null)
    val current: StateFlow<Profile?> = _current.asStateFlow()

    val profiles: StateFlow<List<Profile>> = ProfileRepository.profiles

    init {
        Logger.writeLog("MainViewModel init")
        ProfileRepository.initialize(application)

        viewModelScope.launch {
            ProfileRepository.profiles.collect { list ->
                val selected = list.firstOrNull { it.selected }
                _current.value = selected
                Logger.writeLog("Profiles updated: ${list.size}, selected: ${selected?.name}")
                // اگر selected وجود ندارد و لیست خالی نیست، اولین را انتخاب کن
                if (list.isNotEmpty() && selected == null) {
                    select(list.first().id)
                }
            }
        }

        V2RayService.observeState { newState ->
            _state.value = newState
        }
    }

    fun getSelectedProfile(): Profile? {
        val profile = _current.value
        Logger.writeLog("getSelectedProfile: ${profile?.name}")
        return profile
    }

    fun connect(profile: Profile) {
        Logger.writeLog("connect: ${profile.name}")
        V2RayService.start(getApplication(), profile)
    }

    fun disconnect() {
        Logger.writeLog("disconnect")
        V2RayService.stop(getApplication())
    }

    fun select(id: String) {
        Logger.writeLog("select profile: $id")
        ProfileRepository.select(id)
        _current.value = ProfileRepository.getSelected()
    }

    fun add(profile: Profile) {
        Logger.writeLog("add profile: ${profile.name}")
        ProfileRepository.add(profile)
        // بعد از اضافه کردن، پروفایل جدید را انتخاب کن
        _current.value = ProfileRepository.getSelected()
    }

    fun update(profile: Profile) {
        Logger.writeLog("update profile: ${profile.name}")
        ProfileRepository.update(profile)
        _current.value = ProfileRepository.getSelected()
    }

    fun delete(id: String) {
        Logger.writeLog("delete profile: $id")
        ProfileRepository.delete(id)
        _current.value = ProfileRepository.getSelected()
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}
