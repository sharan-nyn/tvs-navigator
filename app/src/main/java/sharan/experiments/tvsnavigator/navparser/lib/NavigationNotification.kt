/* -*- mode: C++; c-basic-offset: 4; indent-tabs-mode: nil; -*- */
/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * Copyright (c) 2020 Marco Trevisan <mail@trevi.me>
 */

package sharan.experiments.tvsnavigator.navparser.lib

import android.app.Notification
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import android.widget.Button
import sharan.experiments.tvsnavigator.navparser.lib.NavigationData
import sharan.experiments.tvsnavigator.navparser.lib.NavigationTimestamp

open class NavigationNotification(cx: Context, sbn: StatusBarNotification) {
    protected val mNotification : Notification = sbn.notification
    protected val mCx = cx
    protected var mAppSrcCx: Context = mCx.createPackageContext(
        if (Build.VERSION.SDK_INT >= 31) SYSTEM_UI_PACKAGE_NAME else sbn.packageName,
        Context.CONTEXT_IGNORE_SECURITY,
    )

    var navigationData : NavigationData = NavigationData()
        private set
    var stopButton : Button? = null
        protected set

    init {
        navigationData.postTime = NavigationTimestamp(sbn.postTime)
    }

    fun close() {
        stopButton?.callOnClick()
        navigationData.actionIcon.bitmap?.recycle()
    }

    companion object {
        private const val SYSTEM_UI_PACKAGE_NAME = "com.android.systemui"
    }
}
