import SwiftUI
import ComposeApp

class SwiftUINativeViewFactory : NativeViewFactory {
    func createMapView(coordinate: MapCoordinates, title: String) -> (view: UIViewController, delegate: any MapViewDelegate) {
        let observable = MapViewObservable(coordinate: coordinate, title: title)
        let view = NativeMapViewBinding(observable: observable)
        return (view: UIHostingController(rootView: view), delegate: observable)
    }
    
    func createMapViewWithUiView(coordinate: MapCoordinates, title: String) -> (view: UIView, delegate: any MapViewWithUiViewDelegate) {
        let view = NativeMapUIKitView(title: title, coordinate: coordinate)
        return (view: view, delegate: view)
    }
    
    func createMapViewWithSwiftUI(observable: ComposeApp.MapViewWithSwiftUIObservable) -> AnyView {
        return AnyView(NativeMapViewBindingSwiftUI(observable: observable))
    }
}
