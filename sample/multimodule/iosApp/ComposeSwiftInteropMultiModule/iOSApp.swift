import SwiftUI
import ComposeApp

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
        MainComposableViewController(
            listFeatureNativeViews: iOSListViewFactory.shared,
            detailFeatureNativeViews: iOSDetailViewFactory.shared
        )
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}

