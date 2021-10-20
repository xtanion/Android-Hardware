package com.xtanion.bluetoothwidget

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.xtanion.bluetoothwidget.databinding.DevicesColumnBinding
import java.lang.Exception

class DevicesAdapter(val rvInterface: HomeFragment): RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder>() {

    private var devicesList: List<BluetoothDevice> = emptyList()
    private var _binding:DevicesColumnBinding? = null
    private val binding get() = _binding!!

    inner class DevicesViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                rvInterface.onDeviceClick(devicesList[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        _binding = DevicesColumnBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DevicesViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        val device: BluetoothDevice = devicesList[position]
        binding.deviceName.text = device.name
        val deviceTypeImage: Int = when (device.bluetoothClass.deviceClass){
            BluetoothClass.Device.PHONE_SMART -> R.drawable.ic_round_phone_iphone_24
            BluetoothClass.Device.COMPUTER_LAPTOP -> R.drawable.ic_laptop
            BluetoothClass.Device.COMPUTER_DESKTOP -> R.drawable.ic_laptop
            BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> R.drawable.ic_round_headset_24
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET -> R.drawable.ic_round_headset_24
            BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO -> R.drawable.ic_round_headset_24
            BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE -> R.drawable.ic_round_speaker_24
            BluetoothClass.Device.WEARABLE_WRIST_WATCH -> R.drawable.ic_watch
            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> R.drawable.ic_round_speaker_24
            else -> R.drawable.ic_round_wifi_tethering_24
        }
        binding.deviceIcon.setImageResource(deviceTypeImage)


        try {
            val battery = getBatteryLevel(device)
            binding.deviceBattery.apply {
                text = "Battery ${battery}%"
                if (battery!=-1) {
                    visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            Log.d("BatteryPercentage", "Failed to get Battery percentage of ${device.name}")
        }


    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    private fun getBatteryLevel(pairedDevice: BluetoothDevice?): Int {
        return pairedDevice?.let { bluetoothDevice ->
            (bluetoothDevice.javaClass.getMethod("getBatteryLevel"))
                .invoke(pairedDevice) as Int
        } ?: -1
    }


    fun changedData(newList:List<BluetoothDevice>){
        val diffUtil = DevicesDiffUtil(this.devicesList,newList)
        val diffResults = DiffUtil.calculateDiff(diffUtil)
        this.devicesList = newList
        diffResults.dispatchUpdatesTo(this)
    }

    interface devicesRVInterface{
        fun onDeviceClick(data: BluetoothDevice)
    }
}