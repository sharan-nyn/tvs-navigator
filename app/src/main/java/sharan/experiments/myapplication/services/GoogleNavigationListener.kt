package sharan.experiments.myapplication.services

import android.content.Intent
import me.trevi.navparser.lib.NavigationNotification
import me.trevi.navparser.service.NAVIGATION_DATA
import me.trevi.navparser.service.NavigationListener

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