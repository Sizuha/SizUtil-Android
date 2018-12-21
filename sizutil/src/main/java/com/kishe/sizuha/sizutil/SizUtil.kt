package com.kishe.sizuha.sizutil

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import java.util.ArrayList


@Throws(PackageManager.NameNotFoundException::class)
fun getAppVersion(c: Context): String {
    val pInfo = c.packageManager.getPackageInfo(c.packageName, 0)
    return pInfo.versionName
}


class SizAppPermission {
    var permission: String = ""
    var description: String = ""

    constructor() {}

    constructor(permission: String, description: String) {
        this.permission = permission
        this.description = description
    }
}

fun checkPermissions(a: Activity, permissions: List<SizAppPermission>): List<SizAppPermission> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return ArrayList(0)
    }

    val requestList = ArrayList<SizAppPermission>(permissions.size)

    for (p in permissions) {
        if (p.permission.isEmpty()) continue

        if (a.checkSelfPermission(p.permission) != PackageManager.PERMISSION_GRANTED) {
            requestList.add(p)
        }
    }

    return requestList
}

fun requestPermisions(a: Activity, permissions: List<SizAppPermission>, requestCode: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return
    }

    val p_array = arrayOfNulls<String>(permissions.size)

    var i = 0
    for (p in permissions) {
        p_array[i++] = p.permission
    }

    if (p_array.size > 0) ActivityCompat.requestPermissions(a, p_array, requestCode)
}

fun checkRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray): Array<String> {
    val notGranted = ArrayList<String>(grantResults.size)

    val i = 0
    for (p in permissions) {
        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
            notGranted.add(p)
        }
    }

    return notGranted.toTypedArray()
}


/**
 *
 * require a permission: "android.permission.ACCESS_WIFI_STATE"
 *
 * @param context Context
 * @return mac addressを返す。失敗したらnullを返す。
 */
@SuppressLint("HardwareIds")
fun getMacAddr(context: Context): String? {
    val wifiManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        context.getSystemService(WifiManager::class.java)
    }
    else {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    val wInfo = wifiManager?.connectionInfo ?: return null
    return wInfo.macAddress
}