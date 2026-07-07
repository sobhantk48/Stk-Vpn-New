package com.v2ray.app.bg

import android.os.RemoteCallbackList
import android.os.RemoteException
import androidx.lifecycle.MutableLiveData
import com.v2ray.app.aidl.IService
import com.v2ray.app.aidl.IServiceCallback

class ServiceBinder : IService.Stub() {
    companion object {
        const val STATUS_STOPPED = 0
        const val STATUS_STARTING = 1
        const val STATUS_STARTED = 2
        const val STATUS_STOPPING = 3
        const val STATUS_ERROR = 4
    }

    private val callbacks = RemoteCallbackList<IServiceCallback>()
    private val status = MutableLiveData(STATUS_STOPPED)
    private var errorMessage: String? = null
    private var statusCallback: ((String) -> Unit)? = null

    init {
        status.observeForever { state ->
            broadcast { callback ->
                try {
                    callback.onServiceStatusChanged(state)
                } catch (e: RemoteException) {
                    // ignore
                }
            }
            statusCallback?.invoke(getStatusString(state))
        }
    }

    fun setStatusCallback(callback: (String) -> Unit) {
        statusCallback = callback
    }

    fun setStatus(statusText: String) {
        val state = when (statusText.lowercase()) {
            "starting" -> STATUS_STARTING
            "connected", "started" -> STATUS_STARTED
            "stopping" -> STATUS_STOPPING
            "disconnected", "stopped" -> STATUS_STOPPED
            else -> STATUS_STOPPED
        }
        status.postValue(state)
    }

    fun setError(error: String) {
        errorMessage = error
        status.postValue(STATUS_ERROR)
    }

    override fun getStatus(): Int = status.value ?: STATUS_STOPPED

    override fun getErrorMessage(): String = errorMessage ?: ""

    override fun registerCallback(callback: IServiceCallback?) {
        callback?.let { callbacks.register(it) }
    }

    override fun unregisterCallback(callback: IServiceCallback?) {
        callback?.let { callbacks.unregister(it) }
    }

    private fun broadcast(work: (IServiceCallback) -> Unit) {
        val count = callbacks.beginBroadcast()
        try {
            repeat(count) { i ->
                try {
                    work(callbacks.getBroadcastItem(i))
                } catch (_: Exception) {
                    // ignore
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }

    private fun getStatusString(state: Int): String {
        return when (state) {
            STATUS_STOPPED -> "Stopped"
            STATUS_STARTING -> "Starting"
            STATUS_STARTED -> "Started"
            STATUS_STOPPING -> "Stopping"
            STATUS_ERROR -> "Error"
            else -> "Unknown"
        }
    }
}
