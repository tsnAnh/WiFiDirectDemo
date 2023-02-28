package dev.tsnanh.android.wifidirectdemo.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.tsnanh.android.wifidirectdemo.navigation.WiFiDirectDemoDestination
import dev.tsnanh.android.wifidirectdemo.state.LocalWiFiDirectDemoAppState
import dev.tsnanh.android.wifidirectdemo.utils.buildWifiP2PConfig
import dev.tsnanh.android.wifidirectdemo.utils.connect
import dev.tsnanh.android.wifidirectdemo.utils.discoverPeers
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
    val devices by appState.wifiDevices.collectAsState()
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
                items(devices, key = { it.deviceAddress }) {
                    Text(
                        text = it.deviceName,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .clickable {
                                appState.coroutineScope.launch {
                                    val config = buildWifiP2PConfig {
                                        deviceAddress = it.deviceAddress
                                    }
                                    appState.wifiP2pManager
                                        ?.connect(appState.channel, config)
                                        ?.onSuccess { appState.showSnackBar("Connected") }
                                }
                            }
                    )
                }
            }
        }
    }
}
