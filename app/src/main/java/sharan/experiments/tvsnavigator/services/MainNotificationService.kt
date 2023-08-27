package sharan.experiments.tvsnavigator.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.IBinder
import android.provider.ContactsContract
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import sharan.experiments.tvsnavigator.App.Companion.CHANNEL_ID
import sharan.experiments.tvsnavigator.MainActivity
import sharan.experiments.tvsnavigator.R
import sharan.experiments.tvsnavigator.TVSHandler
import sharan.experiments.tvsnavigator.navparser.lib.NavigationData
import sharan.experiments.tvsnavigator.navparser.service.NAVIGATION_DATA
import sharan.experiments.tvsnavigator.utils.Direction
import sharan.experiments.tvsnavigator.utils.DirectionImageMapper


class MainNotificationService : Service() {
    private val navigationServiceBroadcastReceiver = NavigationServiceBroadcastReceiver()

    private lateinit var phoneListener: PhoneListener
    private var phoneListenerCount = 0
    private var tvsHandlerInit = false

    private lateinit var directionImageMapper: DirectionImageMapper
    private lateinit var tvsHandler: TVSHandler

    override fun onCreate() {
        super.onCreate()
        directionImageMapper = DirectionImageMapper(applicationContext)
        phoneListener = PhoneListener()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TVS Navigation Service")
            .setContentText("Ready to start navigation")
            .setSmallIcon(R.drawable.ic_headlamp_logo_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        val intentFilter = IntentFilter()
        intentFilter.addAction(NAVDATA_UPDATED)
        intentFilter.addAction(CONNECT_BT)
        intentFilter.addAction(NAVDATA_REMOVED)
        intentFilter.addAction(START_NAV)
        intentFilter.addAction(STOP_NAV)

        registerReceiver(navigationServiceBroadcastReceiver, intentFilter)

        val tm = applicationContext.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(navigationServiceBroadcastReceiver)
        if (tvsHandlerInit) {
            tvsHandler.stopNavigation()
            tvsHandler.stopConnectionAlive()
            tvsHandler.cancelConnections()
        }
        super.onDestroy()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    inner class NavigationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                NAVDATA_UPDATED -> {
                    val navData = intent.getParcelableExtra<NavigationData>(NAVIGATION_DATA)
                    Log.d(receiverTag, "got nav data: $navData")
                    if (navData != null) {
                        processNavigationData(navData)
                    }
                }

                CONNECT_BT -> {
                    Log.d(receiverTag, "got connect BT request")
                    initTVSHandler()

                    if (tvsHandlerInit) {
                        tvsHandler.connectToTVS()
                        tvsHandler.keepConnectionAlive()
                    }
                }

                START_NAV -> {
                    if (tvsHandlerInit)
                        tvsHandler.startNavigation()
                }

                STOP_NAV -> {
                    if (tvsHandlerInit)
                        tvsHandler.stopNavigation()
                }
            }

        }
    }

    private fun initTVSHandler() {
        val address = getDeviceAddress()
        if (!address.isNullOrEmpty()) {
            tvsHandler = TVSHandler(applicationContext, address)
            tvsHandlerInit = true
        }
    }

    private fun getDeviceAddress(): String? {
        val sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        return sharedPreferences.getString(getString(R.string.device_address), null)
    }

    private fun processNavigationData(navData: NavigationData) {
        val isRerouting = navData.isRerouting
        val direction = navData.actionIcon.bitmap?.let {
            directionImageMapper.getDirectionFromImage(
                it
            )
        }
        val distance = navData.nextDirection.navigationDistance?.distance
        val unit = navData.nextDirection.navigationDistance?.unit
        if (isRerouting) {
            if (tvsHandlerInit)
                tvsHandler.updateAsRerouting()
        } else if (direction != null && distance != null && unit != null) {
            val pairPictogramDirection = Direction.getTVSPictogramAndDirection(direction)
            Log.d("tvsData", "$pairPictogramDirection")
            if (tvsHandlerInit)
                tvsHandler.updateValues(
                    pairPictogramDirection.second,
                    distance,
                    unit,
                    pairPictogramDirection.first,
                    navData.remainingDistance.distance,
                    navData.remainingDistance.unit,
                    navData
                )
        }
    }

    private fun getContactName(phoneNumber: String?, context: Context): String {
        if (phoneNumber.isNullOrEmpty()) return ""

        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName = ""
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }
        return contactName
    }

    inner class PhoneListener : PhoneStateListener() {
        private var tag: String = javaClass.name
        private var isContact = true
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            if (phoneNumber.isNullOrEmpty()) return

            var callerName = getContactName(phoneNumber, applicationContext)
            var myPhoneNumber = phoneNumber

            if (phoneNumber.startsWith("+91") && phoneNumber.length > 10) {
                myPhoneNumber =
                    phoneNumber.substring(3) // reduce number to 10 characters if extension exists
            }

            if (callerName.isEmpty()) {
                isContact = false
                callerName = myPhoneNumber
            } else {
                isContact = true
            }

            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    phoneListenerCount++
                    if (phoneListenerCount >= 1) {
                        phoneListenerCount = 0
                        // Send SMS for missed call.
                        if (tvsHandlerInit)
                            tvsHandler.stopShowingIncomingCallMessage(phoneNumber, isContact)
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    phoneListenerCount = -1
                    if (tvsHandlerInit)
                        tvsHandler.stopShowingIncomingCallMessage("", isContact)
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    phoneListenerCount++
                    if (phoneListenerCount >= 1) {
                        phoneListenerCount = 0
                        // Send signal for showing incoming message.
                        Log.d(tag, "Incoming call - Name: $callerName, Number: $myPhoneNumber")
                        if (tvsHandlerInit)
                            tvsHandler.startShowingIncomingCallMessage(callerName)
                    }
                }
            }
        }
    }

    companion object {
        const val receiverTag = "NavigationServiceBroadcastReceiver"
        const val NAVDATA_UPDATED = "NAVDATA_UPDATED"
        const val CONNECT_BT = "CONNECT_BT"
        const val NAVDATA_REMOVED = "NAVDATA_REMOVED"
        const val START_NAV = "START_NAV"
        const val STOP_NAV = "STOP_NAV"
    }
}
