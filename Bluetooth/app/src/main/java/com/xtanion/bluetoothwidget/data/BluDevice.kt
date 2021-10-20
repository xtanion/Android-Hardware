package com.xtanion.bluetoothwidget.data

import android.bluetooth.BluetoothDevice

data class BluDevice(
    val device: BluetoothDevice,
    val batteryLevel: Int? = null,
    val connected:Boolean? = false
)