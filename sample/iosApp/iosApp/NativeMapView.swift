//
//  NativeMapView.swift
//  iosApp
//
//  Created by Gabriel Souza on 30/04/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp
import MapKit

public class MapViewObservable : MapViewDelegate, ObservableObject {

  @Published
  public var coordinate: MapCoordinates
  @Published
  public var title: String

  public init(coordinate: MapCoordinates, title: String) {
    self.coordinate = coordinate
    self.title = title
  }

  public func updateCoordinate(coordinate: MapCoordinates) {
    self.coordinate = coordinate
  }

  public func updateTitle(title: String) {
    self.title = title
  }
}

struct NativeMapViewBindingSwiftUI : View {
    @ObservedObject var observable: MapViewWithSwiftUIObservable
    
    var body: some View {
        NativeMapView(title: observable.title, coordinate: observable.coordinate)
    }
}

struct NativeMapViewBinding : View {
    @ObservedObject var observable: MapViewObservable
    
    var body: some View {
        NativeMapView(title: observable.title, coordinate: observable.coordinate)
    }
}

struct NativeMapView : View {
    let title: String
    let coordinate: MapCoordinates
    
    @State private var position: MapCameraPosition

    init(title: String, coordinate: MapCoordinates) {
        self.title = title
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
                    title,
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
        .onChange(of: coordinate) { newCoordinate in
            position = .region(
                MKCoordinateRegion(
                    center: CLLocationCoordinate2D(latitude: newCoordinate.lat, longitude: newCoordinate.lng),
                    span: MKCoordinateSpan(latitudeDelta: 0.008, longitudeDelta: 0.008)
                )
            )
        }
    }
}
