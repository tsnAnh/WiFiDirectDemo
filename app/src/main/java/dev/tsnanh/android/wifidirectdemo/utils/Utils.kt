package dev.tsnanh.android.wifidirectdemo.utils

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun WifiP2pManager.createGroup(channel: Channel) = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine<Result<Unit>> { cont ->
        createGroup(channel, object : ActionListener {
            override fun onSuccess() {
                cont.resume(Result.success(Unit))
            }

            override fun onFailure(p0: Int) {
                cont.resume(Result.failure(RuntimeException("Cannot create group")))
            }
        })
    }
}

suspend fun WifiP2pManager.discoverPeers(channel: Channel) = withContext(Dispatchers.IO) {
    suspendCoroutine<Result<Unit>> { cont ->
        discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d("WiFi-Direct Demo", "onSuccess: Discover peers")
                cont.resume(Result.success(Unit))
            }

            override fun onFailure(p0: Int) {
                Log.d("WiFi-Direct Demo", "onFailure: Discover peers")
                cont.resume(Result.failure(RuntimeException("Cannot discover peers")))
            }
        })
    }
}

suspend fun WifiP2pManager.connect(
    channel: Channel,
    config: WifiP2pConfig,
) = withContext(Dispatchers.IO) {
    suspendCoroutine<Result<Unit>> { cont ->
        connect(channel, config, object : ActionListener {
            override fun onSuccess() {
                cont.resume(Result.success(Unit))
            }

            override fun onFailure(p0: Int) {
                cont.resume(Result.failure(RuntimeException("cannot connect")))
            }
        })
    }
}

suspend fun WifiP2pManager.removeGroup(channel: Channel) = withContext(Dispatchers.IO) {
    suspendCoroutine<Result<Unit>> { cont ->
        removeGroup(channel, object : ActionListener {
            override fun onSuccess() {
                cont.resume(Result.success(Unit))
            }

            override fun onFailure(p0: Int) {
                cont.resume(Result.failure(RuntimeException("cannot remove group")))
            }
        })
    }
}

suspend fun WifiP2pManager.requestPeers(channel: Channel) = withContext(Dispatchers.IO) {
    suspendCoroutine<Result<WifiP2pDeviceList>> { cont ->
        requestPeers(channel) {
            cont.resume(Result.success(it))
        }
    }
}

val WifiP2pDevice.statusString get() = when (status) {
    WifiP2pDevice.AVAILABLE -> "Available"
    WifiP2pDevice.CONNECTED -> "Connected"
    WifiP2pDevice.FAILED -> "Failed"
    WifiP2pDevice.INVITED -> "Invited"
    WifiP2pDevice.UNAVAILABLE -> "Unavailable"
    else -> "Unknown"
}
