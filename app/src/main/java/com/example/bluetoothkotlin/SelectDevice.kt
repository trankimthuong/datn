package com.example.bluetoothkotlin

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_select_device.*
class SelectDevice : AppCompatActivity() {

    var m_bluetoothAdapter: BluetoothAdapter? = null
    //lateinit var m_pairedDevice: Set<BluetoothAdapter>
    val REQUEST_ENALBLE_BLUETOOTH = 1



    companion object{
        val EXTRA_ADRESS: String = "Device_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null){
            Toast.makeText(this, "this device doesn't support bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENALBLE_BLUETOOTH)
        }

        select_device_refresh.setOnClickListener {
            pairedDevicesList()
        }
    }

    private fun pairedDevicesList(){
        var m_pairedDevice = m_bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()
        if(!m_pairedDevice.isEmpty()){
            for(device: BluetoothDevice in m_pairedDevice){
                list.add(device)
                Log.i("device", ""+device)
            }
        }else{
            Toast.makeText(this, "no paired bluetooth devices found", Toast.LENGTH_LONG).show()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADRESS, address)
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENALBLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK){
                if(m_bluetoothAdapter!!.isEnabled){
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_LONG).show()
                }
            } else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Bluetooth enableing has been canceled", Toast.LENGTH_LONG).show()
            }
        }
    }

}


