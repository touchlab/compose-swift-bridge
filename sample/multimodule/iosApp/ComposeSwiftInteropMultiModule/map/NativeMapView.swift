import SwiftUI
import ComposeApp
import MapKit

struct NativeMapView : View {
    let placeName: String
    let coordinate: CommonMapCoordinates
    
    @State private var position: MapCameraPosition

    init(placeName: String, coordinate: CommonMapCoordinates) {
        self.placeName = placeName
        self.coordinate = coordinate
        _position = State(initialValue: .region(
            MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: coordinate.lat, longitude: coordinate.lng),
                span: MKCoordinateSpan(latitudeDelta: 0.008, longitudeDelta: 0.008)
            )
        ))
    }
    
    var body: some View {
        MapReader { reader in
            Map(position: $position) {
                Annotation(
                    placeName,
                    coordinate: CLLocationCoordinate2D(
                        latitude: coordinate.lat,
                        longitude: coordinate.lng
                    )
                ) {
                    VStack {
                      Group {
                        Image(systemName: "mappin.circle.fill")
                          .resizable()
                          .frame(width: 30.0, height: 30.0)
                        Circle()
                          .frame(width: 8.0, height: 8.0)
                      }
                      .foregroundColor(.red)
                    }
                }
            }
            .disabled(true)
        }
    }
}

struct NativeMapView_Previews: PreviewProvider {
    static var previews: some View {
        NativeMapView(
            placeName: "Sunder",
            coordinate: CommonMapCoordinates(lat: 55.6259, lng: 12.5723)
        )
    }
}
