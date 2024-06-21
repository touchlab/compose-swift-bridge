//
//  NativeMapUIKitView.swift
//  iosApp
//
//  Created by Gabriel Souza on 21/06/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import UIKit
import MapKit
import ComposeApp

class NativeMapUIKitView: UIView, MapViewWithUiViewDelegate {
    private var mapView: MKMapView!
    private var annotation = MKPointAnnotation()

    init(title: String, coordinate: MapCoordinates, frame: CGRect = .zero) {
        super.init(frame: frame)
        setupMapView(title: title, coordinate: coordinate)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setupMapView(title: String, coordinate: MapCoordinates) {
        mapView = MKMapView()
        mapView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(mapView)
        NSLayoutConstraint.activate([
            mapView.topAnchor.constraint(equalTo: topAnchor),
            mapView.bottomAnchor.constraint(equalTo: bottomAnchor),
            mapView.leadingAnchor.constraint(equalTo: leadingAnchor),
            mapView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
        mapView.isUserInteractionEnabled = false
        updateRegion(for: coordinate)
        addAnnotation(title: title, coordinate: coordinate)
    }

    private func updateRegion(for coordinate: MapCoordinates) {
        let region = MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: coordinate.lat, longitude: coordinate.lng),
            span: MKCoordinateSpan(latitudeDelta: 0.008, longitudeDelta: 0.008)
        )
        mapView.setRegion(region, animated: true)
    }

    private func addAnnotation(title: String, coordinate: MapCoordinates) {
        annotation.title = title
        annotation.coordinate = CLLocationCoordinate2D(latitude: coordinate.lat, longitude: coordinate.lng)
        mapView.addAnnotation(annotation)
    }

    // MARK: - MapViewWithUiViewDelegate
    func updateCoordinate(coordinate: MapCoordinates) {
        annotation.coordinate = CLLocationCoordinate2D(latitude: coordinate.lat, longitude: coordinate.lng)
        updateRegion(for: coordinate)
    }

    func updateTitle(title: String) {
        annotation.title = title
    }
}
