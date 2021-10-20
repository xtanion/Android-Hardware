package com.xtanion.bluetoothwidget

import android.bluetooth.*
import android.companion.BluetoothDeviceFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xtanion.bluetoothwidget.databinding.FragmentHomeBinding


class HomeFragment : Fragment(), DevicesAdapter.devicesRVInterface {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var devicesRVAdapter:DevicesAdapter
    private lateinit var connectedRVAdapter:DevicesAdapter
    // Used in place of startActivityForResult, which is deprecated.
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val availableBTDevices = mutableListOf<BluetoothDevice>()
    private val connectedBTDevices = mutableListOf<BluetoothDevice>()
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action!!) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (!availableBTDevices.contains(device)) {
                        availableBTDevices.add(device)
                    }
                    devicesRVAdapter.changedData(availableBTDevices)
                }
                BluetoothDevice.ACTION_ACL_CONNECTED ->{
                    val device:BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (!connectedBTDevices.contains(device)) {
                        connectedBTDevices.add(device)
                    }
                    connectedRVAdapter.changedData(connectedBTDevices)
                    if (availableBTDevices.contains(device)){
                        availableBTDevices.remove(device)
                        devicesRVAdapter.changedData(availableBTDevices)
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED ->{
                    val device:BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (connectedBTDevices.contains(device)) {
                        connectedBTDevices.remove(device)
                        connectedRVAdapter.changedData(connectedBTDevices)
                    }

                    if (!availableBTDevices.contains(device)){
                        availableBTDevices.add(device)
                        devicesRVAdapter.changedData(availableBTDevices)
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED->{
                    val device:BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    if (connectedBTDevices.contains(device)) {
                        connectedBTDevices.remove(device)
                    }else{
                        connectedBTDevices.add(device)
                    }
                    connectedRVAdapter.changedData(connectedBTDevices)
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Recyclerview Implementation for disconnected
        val devicesRV = binding.devicesRecyclerView
        devicesRVAdapter = DevicesAdapter(this)
        devicesRV.adapter = devicesRVAdapter
        devicesRV.layoutManager = LinearLayoutManager(context)
        devicesRV.setHasFixedSize(false)
        // RV for connected
        val connectedDevicesRV = binding.connectedRecyclerView
        connectedRVAdapter = DevicesAdapter(this)
        connectedDevicesRV.adapter = connectedRVAdapter
        connectedDevicesRV.layoutManager = LinearLayoutManager(context)
        connectedDevicesRV.setHasFixedSize(false)
        //Discover Devices
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.EXTRA_DEVICE)
        context?.registerReceiver(receiver, filter)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>() {
                if (it.resultCode == RESULT_OK) {
                    Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_SHORT).show()
                } else {
                    if (it.resultCode == RESULT_CANCELED) {
                        Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        binding.bluetoothSwitch.apply {
            isChecked = bluetoothAdapter.isEnabled
        }
        binding.bluetoothSwitch.setOnCheckedChangeListener { btn, _isChecked ->
            val enable = enableDisableBT(bluetoothAdapter)
            btn.isChecked = enable
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach {device ->
            connectedBTDevices.add(device)
            connectedRVAdapter.changedData(connectedBTDevices)
        }
        bluetoothAdapter.startDiscovery()
        binding.DiscoverDevicesTitle.setOnClickListener {
            bluetoothAdapter.startDiscovery()
        }
    }

    private fun enableDisableBT(BTAdapter: BluetoothAdapter):Boolean{
        return if (!BTAdapter.isEnabled){
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activityResultLauncher.launch(intent)
            true

        }else{
            BTAdapter.disable()
            false
        }
    }



    companion object {
        const val BLUETOOTH_REQ_CODE = 200
        const val RESULT_OK = -1
        const val RESULT_CANCELED = 0

    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(receiver)
    }

    override fun onDeviceClick(data: BluetoothDevice) {
        Toast.makeText(context,data.address.toString(),Toast.LENGTH_LONG).show()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode== RESULT_OK){
//            Toast.makeText(context, "Bluetooth turned on",Toast.LENGTH_SHORT).show()
//        }else{
//            if (resultCode == RESULT_CANCELED){
//                Toast.makeText(context,"Canceled",Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}