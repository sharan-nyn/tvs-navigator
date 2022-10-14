package sharan.experiments.myapplication.services

import android.content.Intent
import sharan.experiments.myapplication.navparser.lib.NavigationNotification
import sharan.experiments.myapplication.navparser.service.NAVIGATION_DATA
import sharan.experiments.myapplication.navparser.service.NavigationListener

class GoogleNavigationListener : NavigationListener() {
    override fun onCreate() {
        enabled = true
        super.onCreate()
    }

    override fun onNavigationNotificationUpdated(navNotification: NavigationNotification) {
        val intent = Intent(MainNotificationService.NAVDATA_UPDATED)
        intent.putExtra(NAVIGATION_DATA, navNotification.navigationData)
        sendBroadcast(intent)
        super.onNavigationNotificationUpdated(navNotification)
    }
}