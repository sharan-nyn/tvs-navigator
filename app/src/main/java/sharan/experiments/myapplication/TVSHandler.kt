package sharan.experiments.myapplication

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.welie.blessed.BluetoothBytesParser
import me.trevi.navparser.lib.DistanceUnit
import me.trevi.navparser.lib.NavigationData
import sharan.experiments.myapplication.utils.BluetoothHandler
import java.util.*


class TVSHandler(context: Context, deviceAddress: String) {
    private var bluetoothHandler: BluetoothHandler = BluetoothHandler(context, deviceAddress)
    private var encodedN = "4e"
    private var encodedO = "4f"
    private var encodedDollar = "24"

    private var navData: NavigationData? = null
    private var direction = ""
    private var pictogramId = 0
    private var nextDirectionDistance = 0.0
    private var nextDirectionUnit = DistanceUnit.M

    private var remainingDistance = 0.0
    private var remainingDistanceUnit = DistanceUnit.M
    private var count = 0

    private var navTimer = Timer()
    private var callTimer = Timer()
    private var aliveTimer = Timer()

    private var isNavigating = false
    private var isIncomingCall = false

    var smsManager: SmsManager = SmsManager.getDefault()

    private fun getHexString(str: String): String {
        var hex = ""
        for (c in str) {
            hex += "%02x".format(c.toInt())
        }
        return hex
    }

    private fun writeNavigationInstructionsToDevice(){
        if (nextDirectionUnit == DistanceUnit.M && nextDirectionDistance > 300 || nextDirectionUnit == DistanceUnit.KM) {
            count ++
        } else {
            count = 1
        }

        when (count) {
            in 30..40 -> {
                writeRemainingDistanceInstruction()
            }
            in 70..100 -> {
                writeETAInstruction()
            }
            101 -> {
                count = 1
            }
            else -> {
                writeNextDirectionInstruction()
            }
        }
    }

    private fun writeRemainingDistanceInstruction() {
        val message = getHexString("REM. DIST.")
        val multiLine = "01"
        val distanceHex = getDistanceHex(remainingDistance)
        val unitHex = getUnitHex(remainingDistanceUnit)
        val pictogramHex = getPictogramHex(8)
        val instruction = "${encodedN}${distanceHex}${unitHex}${pictogramHex}${multiLine}${message}${encodedDollar}"

        Log.d("plaintext", "REM. DIST. $remainingDistance $remainingDistanceUnit")
        Log.d("instructions", instruction)
        bluetoothHandler.writeToTVS(BluetoothBytesParser.string2bytes(instruction))
    }

    private fun writeETAInstruction() {
        val eta = navData!!.eta.localeString
        val message = getHexString("ETA. $eta")
        val multiLine = "01"
        val distanceHex = getDistanceHex(0.0)
        val unitHex = getUnitHex(DistanceUnit.M)
        val pictogramHex = getPictogramHex(99)
        val instruction = "${encodedN}${distanceHex}${unitHex}${pictogramHex}${multiLine}${message}${encodedDollar}"

        Log.d("plaintext", "REM. DIST. $remainingDistance $remainingDistanceUnit")
        Log.d("instructions", instruction)
        bluetoothHandler.writeToTVS(BluetoothBytesParser.string2bytes(instruction))
    }

    private fun writeNextDirectionInstruction() {
        // do direction processing
        var multiLine = "01"

        var direction1 = direction
        var direction2 = ""

        if (direction.contains(':')) {
            // two instructions - send two BT requests
            val instructions = direction.split(':')
            direction1 = instructions[0]
            direction2 = instructions[1]
            multiLine = "02"
        }

        val unitHex = getUnitHex(nextDirectionUnit)
        val distanceHex = getDistanceHex(nextDirectionDistance)
        val pictogramHex = getPictogramHex(pictogramId)

        // send first instruction
        val encodedMessage1 = getHexString(direction1)
        val instruction1 = "${encodedN}${distanceHex}${unitHex}${pictogramHex}${multiLine}${encodedMessage1}${encodedDollar}"
        Log.d("plaintext1", "$direction1 $nextDirectionDistance $nextDirectionUnit")
        Log.d("instructions1", instruction1)
        bluetoothHandler.writeToTVS(BluetoothBytesParser.string2bytes(instruction1))

        if (multiLine == "02") {
            val encodedMessage2 = getHexString(direction2)
            val instruction2 = "${encodedO}${encodedMessage2}${encodedDollar}"
            Thread.sleep(100)
            bluetoothHandler.writeToTVS(BluetoothBytesParser.string2bytes(instruction2))
            Log.d("plaintext2", direction2)
            Log.d("instructions2", instruction2)

        }
    }

    private fun getPictogramHex(id: Int) = "%02x".format(id)

    private fun getDistanceHex(distance: Double) = "%04x".format((distance * 10).toInt())

    private fun getUnitHex(unit: DistanceUnit): String {
        return if (unit == DistanceUnit.M) {
            "00" // Meters
        } else {
            "01" // Kilometers
        }
    }

    fun updateValues(
            direction: String,
            distance: Double,
            distanceUnit: DistanceUnit,
            pictogramId: Int,
            remainingDistance: Double,
            remainingDistanceUnit: DistanceUnit,
            navData: NavigationData
    ) {
        this.direction = direction
        this.nextDirectionDistance = distance
        this.nextDirectionUnit = distanceUnit
        this.pictogramId = pictogramId
        this.remainingDistance = remainingDistance
        this.remainingDistanceUnit = remainingDistanceUnit
        this.navData = navData
    }

    fun keepConnectionAlive() {
        aliveTimer = Timer()
        aliveTimer.schedule(object : TimerTask() {
            override fun run() {
                if (!isNavigating && !isIncomingCall) bluetoothHandler.writeToTVS(BluetoothBytesParser.string2bytes("4c5b"))
            }
        }, 0, 400)
    }

    fun stopConnectionAlive() {
        aliveTimer.cancel()
    }

    fun updateAsRerouting() {
        nextDirectionDistance = 0.0
        nextDirectionUnit = DistanceUnit.M
        pictogramId = 99
        direction = "REROUTING"
    }

    fun startNavigation() {
        isNavigating = true
        navTimer = Timer()
        navTimer.schedule(object : TimerTask() {
            override fun run() {
                if (!isIncomingCall) writeNavigationInstructionsToDevice()
            }
        }, 0, 400)
    }

    fun stopNavigation() {
        isNavigating = false
        navTimer.cancel()
    }

    fun startShowingIncomingCallMessage(callerName: String) {
        isIncomingCall = true
        val incomingCallDetails = getHexString("C${if (callerName.length > 18) callerName.substring(0, 18) else callerName}$")
        callTimer = Timer()
        callTimer.schedule(object : TimerTask() {
            override fun run() {
                Log.d("incomingCallHandler", callerName)
                bluetoothHandler.writeToTVS(BluetoothBytesParser.string2bytes(incomingCallDetails))
                Thread.sleep(100)
            }
        },0, 400)
    }

    fun stopShowingIncomingCallMessage(phoneNumber: String, isContact: Boolean) {
        isIncomingCall = false
        callTimer.cancel()

        if (phoneNumber.isEmpty()) return

        var smsText = "Thanks for calling, currently I am driving. Will call you after sometime.\n\n - Automatic reply from TVS Apache"
        if (isNavigating && navData != null && navData!!.isValid() && isContact) {
            val eta = navData!!.eta.localeString
            val destination = navData!!.finalDirection
            val currentLocation = navData!!.nextDirection.localeString
            smsText = "Currently I am driving. Will call you after sometime.\n\nIf you are expecting me, currently at $currentLocation. $eta for $destination"
        }
        Log.d("SMS", "Sending SMS $smsText")
        smsManager.sendTextMessage(phoneNumber, null, smsText, null, null)
    }

    fun cancelConnections() {
        bluetoothHandler.cancelConnections()
    }

    fun connectToTVS() {
        bluetoothHandler.connectToTVS()
    }
}