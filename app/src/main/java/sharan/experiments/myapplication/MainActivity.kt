package sharan.experiments.myapplication

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    private var serviceStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectBtn = findViewById(R.id.connectBtn)

        startService = findViewById(R.id.startService)
        stopService = findViewById(R.id.stopService)

        startNavigation = findViewById(R.id.startNavBtn)
        stopNavigation = findViewById(R.id.stopNavBtn)

        initClickListeners()

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = mBluetoothAdapter.bondedDevices
        val listOfPairDevices: MutableList<String> = ArrayList()
        for (bt in pairedDevices) listOfPairDevices.add(bt.name)
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
            if (!serviceStarted) {
                Snackbar.make(it, "Start Service First!", Snackbar.LENGTH_LONG).show()
            } else {
                val intent = Intent(MainNotificationService.CONNECT_BT)
                sendBroadcast(intent)
                Snackbar.make(it, "Connecting to TVS", Snackbar.LENGTH_SHORT).show()
            }
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