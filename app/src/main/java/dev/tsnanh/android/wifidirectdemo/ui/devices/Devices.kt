package dev.tsnanh.android.wifidirectdemo.ui.devices

import android.content.Context
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.tsnanh.android.wifidirectdemo.navigation.WiFiDirectDemoDestination
import dev.tsnanh.android.wifidirectdemo.state.LocalWiFiDirectDemoAppState
import dev.tsnanh.android.wifidirectdemo.utils.connect
import dev.tsnanh.android.wifidirectdemo.utils.discoverPeers
import dev.tsnanh.android.wifidirectdemo.utils.statusString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import kotlin.reflect.typeOf

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
                    ItemDevice(device = it, onClick = {
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
                    })
                }
            }
        }
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        }
    }
    return fileName ?: ""
}

@Composable
fun ItemDevice(device: WifiP2pDevice, onClick: (WifiP2pDevice) -> Unit) {
    val appState = LocalWiFiDirectDemoAppState.current
    val context = LocalContext.current
    val ipAddress by appState.currentIpAddress
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        appState.coroutineScope.launch(Dispatchers.IO) {
            val ip = ipAddress ?: return@launch
            val fd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@launch
            val fileName = getFileNameFromUri(context = context, uri)
            val cacheFile = File(context.cacheDir, fileName)
            FileInputStream(fd.fileDescriptor).use { `is` ->
                cacheFile.writeBytes(`is`.readBytes())
            }
            val socket = Socket(ip.removePrefix("/"), 8888)

            socket.getOutputStream().use { os ->
                os.write(cacheFile.readBytes())
                os.flush()
            }
            fd.close()
            socket.close()
        }
    }
    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .clickable {
            onClick(device)
        }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 24.dp
                    )
                    .weight(1F)
            ) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.subtitle1,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = device.deviceAddress)
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = device.statusString)
            }
            Spacer(modifier = Modifier.size(8.dp))
            IconButton(onClick = {
                filePickerLauncher.launch("*/*")
            }) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = Icons.Rounded.Send.name,
                    tint = Color.Green,
                )
            }
        }
    }
}
