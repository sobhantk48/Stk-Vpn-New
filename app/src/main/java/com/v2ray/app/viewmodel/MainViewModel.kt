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

    fun updateConnectionState(state: ConnectionState) {
        _uiState.value = _uiState.value.copy(connectionState = state)
    }

    data class UiState(
        val profiles: List<Profile> = emptyList(),
        val connectionState: ConnectionState = ConnectionState(status = ConnectionStatus.DISCONNECTED)
    )
}
