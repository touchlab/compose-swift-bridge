package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import data.MapCoordinates
import dev.srsouza.compose.swift.interop.ExpectSwiftView

@ExpectSwiftView
@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    coordinate: MapCoordinates,
    title: String,
)