package dev.tsnanh.android.wifidirectdemo.state

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
class WiFiDirectDemoAppState(
    val navHostController: NavHostController,
    val coroutineScope: CoroutineScope,
    val scaffoldState: ScaffoldState,
    val wifiP2pManager: WifiP2pManager?,
    val channel: Channel,
) {
    private val _devices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val wifiDevices = _devices.asStateFlow()

    fun updateDevices(devices: WifiP2pDeviceList) {
        _devices.update { devices.deviceList.toList() }
    }

    fun showSnackBar(message: String, duration: SnackbarDuration = SnackbarDuration.Short) =
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message = message, duration = duration)
        }
}

@Composable
fun rememberWiFiDirectDemoAppState(
    navHostController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    wifiP2pManager: WifiP2pManager? = LocalContext.current.getSystemService(),
    channel: Channel,
) = remember(navHostController, coroutineScope, scaffoldState, channel) {
    WiFiDirectDemoAppState(navHostController, coroutineScope, scaffoldState, wifiP2pManager, channel)
}
