import SwiftUI
import ComposeApp
import UIKit

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            MainView().ignoresSafeArea(.keyboard)
        }
    }
}

struct MainView : UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        MainViewController(generatedViewFactory: iOSNativeViewFactory(SwiftUINativeViewFactory()))
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}
