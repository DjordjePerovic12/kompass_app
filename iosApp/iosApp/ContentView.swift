import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            // Matches the app's own ink tone (HomePalette.ink) so any gap Compose doesn't
            // explicitly paint shows the app's own dark backdrop instead of SwiftUI's default
            // white, the same fix applied to the Android window background.
            .background(Color(red: Double(0x1E) / 255.0, green: Double(0x1B) / 255.0, blue: Double(0x20) / 255.0))
    }
}



