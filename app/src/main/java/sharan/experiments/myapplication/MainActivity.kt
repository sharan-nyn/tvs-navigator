package sharan.experiments.myapplication

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import sharan.experiments.myapplication.services.MainNotificationService


class MainActivity : AppCompatActivity() {
    private lateinit var connectBtn: Button
    private lateinit var startService: Button
    private lateinit var stopService: Button
    private lateinit var startNavigation: Button
    private lateinit var stopNavigation: Button
    private lateinit var deviceString: TextView

    private var serviceStarted = false
    private var deviceHWAddressAvailable = false

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectBtn = findViewById(R.id.connectBtn)
        deviceString = findViewById(R.id.bluetoothDeviceAddressTV)
        startService = findViewById(R.id.startService)
        stopService = findViewById(R.id.stopService)

        startNavigation = findViewById(R.id.startNavBtn)
        stopNavigation = findViewById(R.id.stopNavBtn)
        sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)

        getDevice()
        initClickListeners()
    }

    private fun initClickListeners() {
        startService.setOnClickListener {
            startService()
            serviceStarted = true
            Snackbar.make(it, "Started Service", Snackbar.LENGTH_SHORT).show()
        }

        stopService.setOnClickListener {
            stopService()
            serviceStarted = false
            Snackbar.make(it, "Stopped Service", Snackbar.LENGTH_SHORT).show()
        }

        connectBtn.setOnClickListener {
            if (!deviceHWAddressAvailable) {
                Snackbar.make(it, "Select Device First!", Snackbar.LENGTH_LONG).show()
                showDeviceDialog()
                return@setOnClickListener
            }

            if (!serviceStarted) {
                Snackbar.make(it, "Start Service First!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(MainNotificationService.CONNECT_BT)
            sendBroadcast(intent)
            Snackbar.make(it, "Connecting to TVS", Snackbar.LENGTH_SHORT).show()

        }

        startNavigation.setOnClickListener {
            if (!serviceStarted) {
                Snackbar.make(it, "Start Service First!", Snackbar.LENGTH_LONG).show()
            } else {
                val intent = Intent(MainNotificationService.START_NAV)
                sendBroadcast(intent)
                Snackbar.make(it, "Started Navigation", Snackbar.LENGTH_SHORT).show()
            }
        }

        stopNavigation.setOnClickListener {
            if (!serviceStarted) {
                Snackbar.make(it, "Start Service First!", Snackbar.LENGTH_LONG).show()
            } else {
                val intent = Intent(MainNotificationService.STOP_NAV)
                sendBroadcast(intent)
                Snackbar.make(it, "Stopped Navigation", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeviceDialog() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = mBluetoothAdapter.bondedDevices
        val pairedDevicesMap:HashMap<String,String> = HashMap()
        for (bt in pairedDevices){
            pairedDevicesMap[bt.name] = bt.address
        }

        val pairedDevicesNamesArrayAdaptor =
            ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_selectable_list_item)
        pairedDevicesNamesArrayAdaptor.addAll(pairedDevicesMap.keys)

        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle("Choose a paired device")
        builder.setAdapter(pairedDevicesNamesArrayAdaptor) { _, which ->
            val device = pairedDevicesNamesArrayAdaptor.getItem(which)
            val address = pairedDevicesMap[device]
            if (device != null && address != null) {
                setDevice(device, address)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setDevice(device:String, address:String) {
        with (sharedPreferences.edit()) {
            putString(getString(R.string.device_name), device)
            putString(getString(R.string.device_address), address)
            apply()
        }
        setDeviceString(device, address)
        deviceHWAddressAvailable = true
    }

    private fun getDevice() {
        val device = sharedPreferences.getString(getString(R.string.device_name), null)
        val address = sharedPreferences.getString(getString(R.string.device_address), null)

        if (!device.isNullOrEmpty() && !address.isNullOrEmpty()) {
            setDeviceString(device, address)
            deviceHWAddressAvailable = true
        }
    }

    private fun setDeviceString(device: String, address: String) {
        val deviceStr = "$device ($address)"
        deviceString.text = deviceStr
    }

    private fun startService() {
        val serviceIntent = Intent(this, MainNotificationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopService() {
        val serviceIntent = Intent(this, MainNotificationService::class.java)
        stopService(serviceIntent)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}