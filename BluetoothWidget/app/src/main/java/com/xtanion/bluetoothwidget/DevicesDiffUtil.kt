package com.xtanion.bluetoothwidget

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import androidx.recyclerview.widget.DiffUtil

class DevicesDiffUtil(
    private val oldList: List<BluetoothDevice>,
    private val newList: List<BluetoothDevice>
):DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].address == newList[newItemPosition].address
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when{
            oldList[oldItemPosition].name == newList[newItemPosition].name -> false
            oldList[oldItemPosition].type == newList[newItemPosition].type -> false
            else -> true

        }
    }
}