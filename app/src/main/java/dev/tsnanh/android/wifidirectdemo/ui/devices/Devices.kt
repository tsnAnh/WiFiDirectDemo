package dev.tsnanh.android.wifidirectdemo.ui.devices

import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.tsnanh.android.wifidirectdemo.navigation.WiFiDirectDemoDestination
import dev.tsnanh.android.wifidirectdemo.state.LocalWiFiDirectDemoAppState
import dev.tsnanh.android.wifidirectdemo.utils.connect
import dev.tsnanh.android.wifidirectdemo.utils.discoverPeers
import dev.tsnanh.android.wifidirectdemo.utils.statusString
import kotlinx.coroutines.launch

object DevicesDestination : WiFiDirectDemoDestination {
    override val route: String = "devices"
}

fun NavGraphBuilder.devices() {
    composable(route = DevicesDestination.route) {
        Devices()
    }
}

@Composable
private fun Devices() {
    val appState = LocalWiFiDirectDemoAppState.current
    val devices by appState.wifiDevices
    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(onClick = {
                appState.coroutineScope.launch {
                    val channel = appState.wifiP2pManager?.discoverPeers(appState.channel)
                    channel?.onSuccess {
                        appState.showSnackBar("Discovery initiated")
                    }
                }
            }) {
                Text(text = "Find devices")
            }
            LazyColumn {
                items(devices.deviceList.toList(), key = { it.deviceAddress }) {
                    Card(modifier = Modifier
                        .fillParentMaxWidth()
                        .clickable {
                            appState.coroutineScope.launch {
                                val config = WifiP2pConfig().apply {
                                    deviceAddress = it.deviceAddress
                                    wps.setup = WpsInfo.PBC
                                }
                                appState.wifiP2pManager
                                    ?.connect(appState.channel, config)
                                    ?.onSuccess { appState.showSnackBar("Connected") }
                                    ?.onFailure { appState.showSnackBar("Connect failed") }
                            }
                        }) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                            Text(
                                text = it.deviceName,
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = it.deviceAddress)
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = it.statusString)
                        }
                    }
                }
            }
        }
    }
}
