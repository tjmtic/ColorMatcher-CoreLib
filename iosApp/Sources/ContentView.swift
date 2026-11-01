import ComposeApp
import SwiftUI
import UIKit

/// The thin Swift shell: hosts the shared Compose UI. Platform services go here, not in Kotlin.
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
