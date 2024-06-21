package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import data.MapCoordinates
import dev.srsouza.compose.swift.interop.ExpectSwiftView
import dev.srsouza.compose.swift.interop.ViewType

@ExpectSwiftView(
    type = ViewType.UIViewController
)
@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    coordinate: MapCoordinates,
    title: String,
)

@ExpectSwiftView(
    type = ViewType.SwiftUI
)
@Composable
expect fun MapViewWithSwiftUI(
    modifier: Modifier = Modifier,
    coordinate: MapCoordinates,
    title: String,
)

@ExpectSwiftView(
    type = ViewType.UIView
)
@Composable
expect fun MapViewWithUiView(
    modifier: Modifier = Modifier,
    coordinate: MapCoordinates,
    title: String,
)