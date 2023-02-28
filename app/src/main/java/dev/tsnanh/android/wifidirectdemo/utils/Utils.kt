package dev.tsnanh.android.wifidirectdemo.utils

import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun WifiP2pManager.createGroup(channel: Channel) = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine<Result<Unit>> { cont ->
        createGroup(channel, object : WifiP2pManager.ActionListener {
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
        discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                cont.resume(Result.success(Unit))
            }

            override fun onFailure(p0: Int) {
                cont.resume(Result.failure(RuntimeException("Cannot discover peers")))
            }
        })
    }
}

data class WFP2PConfigBuilder(
    var wps: WpsInfo? = null,
    var deviceAddress: String? = null,
    var groupOwnerIntent: Int? = null,
) {
    fun toWifiP2PConfig() = WifiP2pConfig().apply {
        this@apply.wps = this@WFP2PConfigBuilder.wps
        this@apply.deviceAddress = this@WFP2PConfigBuilder.deviceAddress
        this@WFP2PConfigBuilder.groupOwnerIntent?.let {
            this@apply.groupOwnerIntent = it
        }
    }
}

fun buildWifiP2PConfig(builder: WFP2PConfigBuilder.() -> Unit) =
    WFP2PConfigBuilder().apply(builder).toWifiP2PConfig()

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
