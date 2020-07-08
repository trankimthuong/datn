package com.example.bluetoothkotlin

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_control.*
import java.io.IOException
import java.util.*

class ControlActivity : AppCompatActivity() {

    companion object{
        var m_myUUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket : BluetoothSocket? = null
        lateinit var m_prograess: ProgressDialog
        lateinit var bAdapter : BluetoothAdapter
        var m_isConnected : Boolean = false
        lateinit var  m_adress : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        m_adress = intent.getStringExtra(SelectDevice.EXTRA_ADRESS)
        ConnectToDevice(this).execute()

        sendstring.setOnClickListener {
            sendCommand(edittext.text.toString())
        }
        control_led_disconnect.setOnClickListener {
            disconnect()
        }
        receiver_data.setOnClickListener {
            sendCommand("c")
            if(m_bluetoothSocket != null){
                try{
                    val available = m_bluetoothSocket!!.inputStream.available()
                    val bytes = ByteArray(available)
                    m_bluetoothSocket!!.inputStream.read(bytes, 0, available)

                    val text = String(bytes)
                    ketqua.text = text
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }

        }
        sendfile.setOnClickListener {
            val intent: Intent = Intent(this, SendFileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendCommand(input: String){
        if(m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun disconnect(){
        if(m_bluetoothSocket != null){
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket == null
                m_isConnected = false
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){
        private var connectSuccess : Boolean = true
        private var context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_prograess = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if(m_bluetoothSocket == null || !m_isConnected){
                    bAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device : BluetoothDevice = bAdapter.getRemoteDevice(m_adress)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            }else{
                m_isConnected = true
            }

            m_prograess.dismiss()
        }
    }
}
