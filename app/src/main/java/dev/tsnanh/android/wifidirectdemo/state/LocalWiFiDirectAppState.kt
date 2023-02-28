package dev.tsnanh.android.wifidirectdemo.state

import androidx.compose.runtime.compositionLocalOf

val LocalWiFiDirectDemoAppState =
    compositionLocalOf<WiFiDirectDemoAppState> { error("Cannot find any app state in composition") }
