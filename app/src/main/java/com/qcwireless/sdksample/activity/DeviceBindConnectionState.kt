package com.qcwireless.sdksample.activity

internal class DeviceBindConnectionState {
    enum class Resolution {
        SUCCESS,
        FAILURE,
        CANCELLED,
        TIMEOUT,
    }

    var isConnecting: Boolean = false
        private set

    private var suppressNextDisconnectEvent: Boolean = false

    fun start(): Boolean {
        if (isConnecting) {
            return false
        }
        isConnecting = true
        suppressNextDisconnectEvent = false
        return true
    }

    fun cancel(): Resolution? {
        if (!isConnecting) {
            return null
        }
        isConnecting = false
        suppressNextDisconnectEvent = true
        return Resolution.CANCELLED
    }

    fun timeout(): Resolution? {
        if (!isConnecting) {
            return null
        }
        isConnecting = false
        suppressNextDisconnectEvent = true
        return Resolution.TIMEOUT
    }

    fun onConnectionEvent(connected: Boolean): Resolution? {
        if (connected) {
            if (!isConnecting) {
                return null
            }
            isConnecting = false
            suppressNextDisconnectEvent = false
            return Resolution.SUCCESS
        }

        if (suppressNextDisconnectEvent) {
            suppressNextDisconnectEvent = false
            return null
        }

        if (!isConnecting) {
            return null
        }

        isConnecting = false
        return Resolution.FAILURE
    }
}
