package dev.tsnanh.android.wifidirectdemo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.tsnanh.android.wifidirectdemo.ui.devices.DevicesDestination
import dev.tsnanh.android.wifidirectdemo.ui.devices.devices

@Composable
fun WiFiDirectDemoApp(navHostController: NavHostController) {
    NavHost(
        navController = navHostController,
        startDestination = DevicesDestination.route,
    ) {
        devices()
    }
}
