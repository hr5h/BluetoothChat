package com.plcoding.bluetoothchat.presentation

import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isStart: Boolean = true,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isSender: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = emptyList()
)
