package com.abyxcz.template

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController

/** Entry point for the Swift shell (iosApp/Sources/ContentView.swift). */
@Suppress("FunctionName") // named for Swift: MainViewControllerKt.MainViewController()
fun MainViewController(): UIViewController = ComposeUIViewController {
    val device = UIDevice.currentDevice
    App(platform = "${device.systemName} ${device.systemVersion}")
}
