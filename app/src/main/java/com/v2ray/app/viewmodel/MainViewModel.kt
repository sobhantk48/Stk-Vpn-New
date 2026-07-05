package com.v2ray.app.viewmodel

import androidx.lifecycle.ViewModel
import com.v2ray.app.data.ConnectionState
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        _uiState.value = _uiState.value.copy(profiles = repository.getProfiles())
    }

    fun updateConnectionState(state: ConnectionState) {
        _uiState.value = _uiState.value.copy(connectionState = state)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            connectionState = _uiState.value.connectionState.copy(errorMessage = null)
        )
    }

    fun connect(profile: Profile) {
        _uiState.value = _uiState.value.copy(
            connectionState = ConnectionState(status = ConnectionStatus.CONNECTING)
        )
        // TODO: پیاده‌سازی اتصال به V2Ray
    }

    fun disconnect() {
        _uiState.value = _uiState.value.copy(
            connectionState = ConnectionState(status = ConnectionStatus.DISCONNECTED)
        )
    }

    fun delete(profile: Profile) {
        val updatedList = _uiState.value.profiles.filter { it.id != profile.id }
        _uiState.value = _uiState.value.copy(profiles = updatedList)
    }

    fun update(profile: Profile) {
        val updatedList = _uiState.value.profiles.map {
            if (it.id == profile.id) profile else it
        }
        _uiState.value = _uiState.value.copy(profiles = updatedList)
    }

    data class UiState(
        val profiles: List<Profile> = emptyList(),
        val connectionState: ConnectionState = ConnectionState(status = ConnectionStatus.DISCONNECTED)
    )
}
