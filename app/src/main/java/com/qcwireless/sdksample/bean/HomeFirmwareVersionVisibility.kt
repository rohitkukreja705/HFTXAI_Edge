package com.qcwireless.sdksample.bean

data class FirmwareVersionUiState(
    val visible: Boolean,
    val firmwareVersion: String
)

object HomeFirmwareVersionVisibility {
    fun resolve(isConnected: Boolean, firmwareVersion: String): FirmwareVersionUiState {
        val normalizedVersion = firmwareVersion.trim()
        if (!isConnected || normalizedVersion.isEmpty()) {
            return FirmwareVersionUiState(
                visible = false,
                firmwareVersion = ""
            )
        }

        return FirmwareVersionUiState(
            visible = true,
            firmwareVersion = normalizedVersion
        )
    }
}
