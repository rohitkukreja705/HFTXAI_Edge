package com.qcwireless.sdksample.bean

data class ConnectionActionUiState(
    val reconnectVisible: Boolean,
    val disconnectVisible: Boolean,
    val btPairVisible: Boolean,
    val unbindVisible: Boolean,
    val findDeviceVisible: Boolean
)

object HomeConnectionActionVisibility {
    fun resolve(isConnected: Boolean, deviceMac: String): ConnectionActionUiState {
        val hasDeviceMac = deviceMac.trim().isNotEmpty()
        if (!hasDeviceMac) {
            return ConnectionActionUiState(
                reconnectVisible = false,
                disconnectVisible = false,
                btPairVisible = false,
                unbindVisible = false,
                findDeviceVisible = false
            )
        }

        return ConnectionActionUiState(
            reconnectVisible = !isConnected,
            disconnectVisible = isConnected,
            btPairVisible = true,
            unbindVisible = true,
            findDeviceVisible = isConnected
        )
    }
}
