import SwiftUI
import ComposeApp

class SwiftUINativeViewFactory : NativeViewFactory {
    func createMapView(observable: MapViewObservable) -> UIViewController {
        let view = NativeMapViewBinding(observable: observable)
        return UIHostingController(rootView: view)
    }
}
