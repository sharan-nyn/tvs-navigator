/* -*- mode: C++; c-basic-offset: 4; indent-tabs-mode: nil; -*- */
/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * Copyright (c) 2020 Marco Trevisan <mail@trevi.me>
 */

package sharan.experiments.myapplication.navparser.service

import android.app.PendingIntent
import android.content.Intent
import sharan.experiments.myapplication.navparser.lib.NavigationNotification
import timber.log.Timber as Log

const val SET_INTENT = "SET_INTENT"
const val INTENT_SET = "INTENT_SET"
const val CHECK_NOTIFICATIONS_ACCESS = "CHECK_NOTIFICATIONS_ACCESS"
const val NOTIFICATIONS_ACCESS_RESULT = "NOTIFICATIONS_ACCESS_RESULT"
const val STOP_NAVIGATION = "STOP_NAVIGATION"
const val NAVIGATION_DATA_UPDATED = "NAVIGATION_DATA_UPDATED"
const val NAVIGATION_STARTED = "NAVIGATION_STARTED"
const val NAVIGATION_STOPPED = "NAVIGATION_STOPPED"

const val PENDING_INTENT = "pendingIntent"
const val NAVIGATION_DATA = "navData"

open class NavigationListenerEmitter : NavigationListener() {
    private var mPendingIntent : PendingIntent? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("onStartCommand: $intent")
        when (intent?.action) {
            SET_INTENT -> {
                mPendingIntent = intent.getParcelableExtra(PENDING_INTENT)
                enabled = mPendingIntent != null
                Log.v("Set pending intent $mPendingIntent: $intent, ${intent.action}")

                if (mPendingIntent != null) {
                    mPendingIntent?.send(applicationContext, 200, Intent(INTENT_SET))
                }
            }

            CHECK_NOTIFICATIONS_ACCESS -> {
                if (mPendingIntent == null) {
                    Log.e("No intent set!")
                    return START_NOT_STICKY
                }

                mPendingIntent!!.send(applicationContext, 200,
                    Intent(NOTIFICATIONS_ACCESS_RESULT).putExtra(
                        NOTIFICATIONS_ACCESS_RESULT,
                        haveNotificationsAccess()))
            }

            STOP_NAVIGATION -> {
                Log.d("Stopping navigation requested: ${currentNotification?.stopButton}")
                if (currentNotification != null)
                    currentNotification!!.close()
            }
        }

        return START_NOT_STICKY
    }

    override fun onNavigationNotificationAdded(navNotification: NavigationNotification) {
        mPendingIntent?.send(applicationContext, 200, Intent(NAVIGATION_STARTED))
    }

    override fun onNavigationNotificationUpdated(navNotification : NavigationNotification) {
        mPendingIntent?.send(
            applicationContext, 200, Intent(NAVIGATION_DATA_UPDATED).putExtra(
                NAVIGATION_DATA, navNotification.navigationData
            )
        )
    }

    override fun onNavigationNotificationRemoved(navNotification : NavigationNotification) {
        enabled = false
        mPendingIntent?.send(applicationContext, 200, Intent(NAVIGATION_STOPPED))
    }
}
