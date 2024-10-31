package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import data.MapCoordinates
import co.touchlab.compose.swift.bridge.ExpectSwiftView
import co.touchlab.compose.swift.bridge.ExpectSwiftViewCustomInteropComposable
import co.touchlab.compose.swift.bridge.ViewType

@ExpectSwiftView(
    type = ViewType.UIViewController,
    keepStateCrossNavigation = true,
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
    callback: (String) -> Unit
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

@ExpectSwiftView(
    type = ViewType.SwiftUI
)
@ExpectSwiftViewCustomInteropComposable("sample.ViewControllerCustom")
@Composable
expect fun MapViewCustomInteropComposable(
    modifier: Modifier = Modifier,
    coordinate: MapCoordinates,
    title: String,
)