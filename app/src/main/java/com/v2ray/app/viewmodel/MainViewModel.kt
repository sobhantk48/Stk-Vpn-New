package com.v2ray.app.viewmodel

import androidx.lifecycle.ViewModel
import com.v2ray.app.data.ConnectionState
import com.v2ray.app.data.ConnectionStatus
import com.v2ray.app.data.Profile
import com.v2ray.app.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        _uiState.update { it.copy(profiles = repository.getProfiles()) }
    }

    fun connect() {
        _uiState.update {
            it.copy(
                connectionState = ConnectionState(
                    status = ConnectionStatus.CONNECTING,
                    ping = 0,
                    downloadSpeed = 0.0,
                    uploadSpeed = 0.0
                )
            )
        }
    }

    fun disconnect() {
        _uiState.update {
            it.copy(
                connectionState = ConnectionState(status = ConnectionStatus.DISCONNECTED)
            )
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                connectionState = it.connectionState.copy(errorMessage = null)
            )
        }
    }

    data class UiState(
        val profiles: List<Profile> = emptyList(),
        val connectionState: ConnectionState = ConnectionState(status = ConnectionStatus.DISCONNECTED)
    )
}
