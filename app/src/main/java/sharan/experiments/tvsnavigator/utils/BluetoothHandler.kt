package sharan.experiments.tvsnavigator.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.welie.blessed.*
import java.util.*


class BluetoothHandler(context: Context, deviceAddress: String) {
    private var centralManager: BluetoothCentralManager
    private val TVS_SERVICE_UUID: UUID = UUID.fromString("00005500-d102-11e1-9b23-000240198212")
    private val TVS_WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("00005502-d102-11e1-9b23-000240198212")

    private var tvsPeripheral: BluetoothPeripheral
    private lateinit var tvsWriteCharacteristic: BluetoothGattCharacteristic

    private var isConnected = false

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
            object : BluetoothCentralManagerCallback() {
                override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
                    super.onConnectedPeripheral(peripheral)
                    isConnected = true
                    Toast.makeText(context, "Connected to device, ${peripheral.name}", Toast.LENGTH_LONG).show()
                    getWriteCharacteristic()
                    initBTConnection()
                }

                override fun onDisconnectedPeripheral(
                        peripheral: BluetoothPeripheral,
                        status: HciStatus
                ) {
                    super.onDisconnectedPeripheral(peripheral, status)
                    isConnected = false
                }
            }

    private val peripheralCallback: BluetoothPeripheralCallback =
            object : BluetoothPeripheralCallback() {
                override fun onCharacteristicWrite(
                        peripheral: BluetoothPeripheral,
                        value: ByteArray,
                        characteristic: BluetoothGattCharacteristic,
                        status: GattStatus
                ) {
                    Log.d("tag", "Write successful ${value.decodeToString()}")
                }

            }

    init {
        centralManager = BluetoothCentralManager(
                context, bluetoothCentralManagerCallback, Handler(Looper.getMainLooper())
        )
        tvsPeripheral = centralManager.getPeripheral(deviceAddress)
    }

    private fun getWriteCharacteristic() {
        tvsWriteCharacteristic =
                tvsPeripheral.getCharacteristic(TVS_SERVICE_UUID, TVS_WRITE_CHARACTERISTIC_UUID)!!
    }

    fun connectToTVS() {
        centralManager.connectPeripheral(tvsPeripheral, peripheralCallback)
        initBTConnection()
    }

    private fun initBTConnection() {
        if (isConnected) {
            tvsPeripheral.writeCharacteristic(tvsWriteCharacteristic, BluetoothBytesParser.string2bytes("0100"), WriteType.WITH_RESPONSE) // request write
            Thread.sleep(1000)
            writeToTVS(BluetoothBytesParser.string2bytes("564170616368652052545224")) // bike name
            writeToTVS(BluetoothBytesParser.string2bytes("580004000201")) // missed calls
            Thread.sleep(400)
            writeToTVS(BluetoothBytesParser.string2bytes("580004000201")) // missed calls
            Thread.sleep(400)
            writeToTVS(BluetoothBytesParser.string2bytes("580004000201")) // missed calls
            Thread.sleep(400)
            writeToTVS(BluetoothBytesParser.string2bytes("5711")) // random stuff I found in code/log
            sendHourMinute()
            sendHourMinute()
            writeToTVS(BluetoothBytesParser.string2bytes(getHexString("MTVS Rider$"))) // rider-name
            sendHourMinute()
            writeToTVS(BluetoothBytesParser.string2bytes(getHexString("MTVS Rider$"))) // rider-name
        }
    }

    private fun sendHourMinute() {
        var i: Int
        val instance = Calendar.getInstance()
        var i2 = instance[Calendar.HOUR_OF_DAY] // Hour/24h format

        val i3 = instance[Calendar.MINUTE] // Minute
        if (i2 == 0) {
            i2 += 12 // convert to 12h time
        } else {
            if (i2 != 12) {
                if (i2 > 12) {
                    i2 -= 12 // convert to 12h time
                }
            }
            i = 1
            val bArr = byteArrayOf(java.lang.Byte.decode(getHexString("H")).toByte(), i2.toByte(), i3.toByte(), i.toByte())
            writeToTVS(bArr)
            Thread.sleep(100)
        }
        i = 0
        val bArr2 = byteArrayOf(java.lang.Byte.decode(getHexString("H")).toByte(), i2.toByte(), i3.toByte(), i.toByte())
        writeToTVS(bArr2)
        Thread.sleep(100)
    }

    private fun getHexString(str: String): String {
        var hex = ""
        for (c in str) {
            hex += "%02x".format(c.toInt())
        }
        return hex
    }

    fun writeToTVS(byteArray: ByteArray) {
        if (!isConnected) connectToTVS()
        if (isConnected) {
            Log.d("tag", "writeToTVS - ${byteArray.decodeToString()}")
            try {
                tvsPeripheral.writeCharacteristic(
                    tvsWriteCharacteristic,
                    byteArray,
                    WriteType.WITHOUT_RESPONSE
                )
            } catch (e: Exception) {
                Log.d("tag", "writeCharacteristic failed. Error: $e")
            }
        }
    }

    fun cancelConnections() {
        centralManager.cancelConnection(tvsPeripheral)
    }

}