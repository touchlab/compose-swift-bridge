import SwiftUI
import ComposeApp

class TestDelegate1 : MapViewDelegate {
    func updateCoordinate(coordinate: MapCoordinates) {
        
    }
    
    func updateTitle(title: String) {
        
    }
}

class SwiftUINativeViewFactory : NativeViewFactory {
    func createMapView(coordinate: MapCoordinates, title: String) -> (view: UIViewController, delegate: any MapViewDelegate) {
        let view = NativeMapView(title: title, coordinate: coordinate)
        return (view: UIHostingController(rootView: view), delegate: TestDelegate1())
    }
    
    func createMapViewWithUiView(coordinate: MapCoordinates, title: String) -> (view: UIView, delegate: any MapViewWithUiViewDelegate) {
        let view = NativeMapUIKitView(title: title, coordinate: coordinate)
        return (view: view, delegate: view)
    }
    
    func createMapViewWithSwiftUI(observable: ComposeApp.MapViewWithSwiftUIObservable) -> AnyView {
        return AnyView(NativeMapViewBindingSwiftUI(observable: observable))
    }
}
