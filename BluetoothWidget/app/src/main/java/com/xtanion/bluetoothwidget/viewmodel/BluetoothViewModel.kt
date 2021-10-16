package com.xtanion.bluetoothwidget.viewmodel

import android.app.Application
import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class BluetoothViewModel(application: Application):AndroidViewModel(application) {
    private var batteryPercentage:Int? = null
    private var bluetoothState:Boolean? = false
    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    private fun getBatteryLevel(pairedDevice: BluetoothDevice): Int{
        return pairedDevice.let { bluetoothDevice ->
            (bluetoothDevice.javaClass.getMethod("getBatteryLevel"))
                .invoke(pairedDevice) as Int
        } ?: -1
    }
}