import Foundation
import SwiftUI
import ComposeApp
import UIKit

class iOSDetailViewFactory : DetailFactory {
    static var shared = iOSDetailViewFactory()

    func createMapView(observable: ComposeApp.MapViewObservable) -> AnyView {
        // TODO: use the observable directly into the view
        return AnyView(NativeMapView(placeName: observable.placeName, coordinate: observable.coordinate))
    }
}

class iOSListViewFactory : ListFactory {
    static var shared = iOSListViewFactory()
    
    func createListScreenContent(observable: ComposeApp.ListScreenContentObservable) -> AnyView {
        // TODO: use the observable directly into the view
        return AnyView(ListScreenContentView(viewModel: observable.viewModel, onRestaurantClick: observable.onRestaurantClick))
    }
}
