package feature.detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.touchlab.compose.expect.swift.ExpectSwiftView
import common.data.MapCoordinates

@Composable
@ExpectSwiftView
expect fun MapView(
    modifier: Modifier,
    placeName: String,
    coordinate: MapCoordinates,
)