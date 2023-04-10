package dev.tsnanh.android.wifidirectdemo

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import dev.tsnanh.android.wifidirectdemo.state.LocalWiFiDirectDemoAppState
import dev.tsnanh.android.wifidirectdemo.state.rememberWiFiDirectDemoAppState
import dev.tsnanh.android.wifidirectdemo.ui.theme.WiFiDirectDemoTheme
import dev.tsnanh.android.wifidirectdemo.utils.SystemBroadcastReceiver
import dev.tsnanh.android.wifidirectdemo.utils.requestPeers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var channel: Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel

        // Request the necessary permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES),
                1
            )
        }
        channel = getSystemService<WifiP2pManager>()?.initialize(this, mainLooper, null)!!

        setContent {
            WiFiDirectDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val appState = rememberWiFiDirectDemoAppState(channel = channel)
                    CompositionLocalProvider(LocalWiFiDirectDemoAppState provides appState) {
                        WiFiDirectDemoApp(appState.navHostController)
                    }

                    val intentFilter = remember {
                        IntentFilter().apply {
                            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
                        }
                    }
                    SystemBroadcastReceiver(intentFilter) { intent ->
                        val action = intent.action
                        if (action != null) {
                            when (action) {
                                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                                    val state =
                                        intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                                        Log.d(TAG, "Wi-Fi Direct is enabled")
                                    } else {
                                        Log.d(TAG, "Wi-Fi Direct is disabled")
                                    }
                                }
                                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> appState.coroutineScope.launch {
                                    val devices = appState.wifiP2pManager?.requestPeers(channel)
                                    devices?.onSuccess { appState.updateDevices(it) }
                                        ?.onFailure {
                                            appState.showSnackBar(
                                                it.message ?: "Unknown exception"
                                            )
                                        }
                                }
                                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                                    val networkInfo =
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                                            intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO, WifiP2pInfo::class.java)
                                        } else {
                                            intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                                        } ?: return@SystemBroadcastReceiver
                                    if (networkInfo.groupOwnerAddress == null) return@SystemBroadcastReceiver
                                    Log.d(TAG, "onCreate: $networkInfo")
                                    appState.updateIpAddress(networkInfo.groupOwnerAddress.hostAddress)
                                }
                                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                                    val device: WifiP2pDevice? =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            intent.getParcelableExtra(
                                                WifiP2pManager.EXTRA_WIFI_P2P_DEVICE,
                                                WifiP2pDevice::class.java
                                            )
                                        } else {
                                            intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                                        }
                                    println(device?.deviceName + " dumami")
                                    // appState.updateSingleDevice(device)
                                    Log.d(TAG, "Device name: " + device!!.deviceName)
                                    Log.d(TAG, "Device address: " + device.deviceAddress)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "test"
    }
}
