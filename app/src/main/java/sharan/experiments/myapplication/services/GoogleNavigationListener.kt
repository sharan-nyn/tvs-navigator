package sharan.experiments.myapplication.services

import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import sharan.experiments.myapplication.R
import sharan.experiments.myapplication.navparser.lib.NavigationNotification
import sharan.experiments.myapplication.navparser.service.NAVIGATION_DATA
import sharan.experiments.myapplication.navparser.service.NavigationListener

class GoogleNavigationListener : NavigationListener() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate() {
        enabled = true
        sharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        super.onCreate()
    }

    override fun onNavigationNotificationAdded(navNotification: NavigationNotification) {
        if (getAutoLaunchEnabledStatus()) {
            startTVSService()
        }
        super.onNavigationNotificationAdded(navNotification)
    }

    override fun onNavigationNotificationUpdated(navNotification: NavigationNotification) {
        val intent = Intent(MainNotificationService.NAVDATA_UPDATED)
        intent.putExtra(NAVIGATION_DATA, navNotification.navigationData)
        sendBroadcast(intent)
        super.onNavigationNotificationUpdated(navNotification)
    }

    override fun onNavigationNotificationRemoved(navNotification: NavigationNotification) {
        super.onNavigationNotificationRemoved(navNotification)
        if (getAutoLaunchEnabledStatus()) {
            stopTVSService()
        }
    }

    private fun getAutoLaunchEnabledStatus() =
        sharedPreferences.getBoolean(getString(R.string.autoLaunchEnabled), false)

    private fun startTVSService(){
        val serviceIntent = Intent(this, MainNotificationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        var intent = Intent(MainNotificationService.CONNECT_BT)
        sendBroadcast(intent)
        intent = Intent(MainNotificationService.START_NAV)
        sendBroadcast(intent)
    }

    private fun stopTVSService(){
        val intent = Intent(MainNotificationService.STOP_NAV)
        sendBroadcast(intent)
        val serviceIntent = Intent(this, MainNotificationService::class.java)
        stopService(serviceIntent)
    }
}