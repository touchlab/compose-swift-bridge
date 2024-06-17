package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.MapCoordinates

@Composable
fun AppScreen() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        var latLng by remember { mutableStateOf<String>("-27.43681478339051, -48.50083474355332") }
        var title by remember { mutableStateOf("Title") }
        var errorCoordinate by remember { mutableStateOf<String?>(null) }
        var coordinate by remember { mutableStateOf<MapCoordinates>(MapCoordinates(-27.43681478339051, -48.50083474355332)) }

        val coordinates = remember(latLng) {
            val splits = latLng.split(",").map { it.trim() }.map { it.toDoubleOrNull() }
            val lat = splits.getOrNull(0)
            val lng = splits.getOrNull(1)
            if(lat != null && lng != null) {
                coordinate = MapCoordinates(lat, lng)
                errorCoordinate = null
            } else {
                errorCoordinate = "Wrong coordiante format"
            }
        }

        Text("Lat, Lng")
        TextField(
            value = latLng,
            onValueChange = { latLng = it },
            isError = errorCoordinate != null,
            supportingText = {
                if(errorCoordinate != null) {
                    Text(errorCoordinate!!)
                }
            }
        )
        Text("Title")
        TextField(
            value = title,
            onValueChange = { title = it },

        )

        MapView(
            modifier = Modifier.fillMaxWidth()
                .height(200.dp),
            coordinate = coordinate,
            title = title,
        )

        Spacer(Modifier.height(8.dp))

        MapViewWithSwiftUI(
            modifier = Modifier.fillMaxWidth()
                .height(200.dp),
            coordinate = coordinate,
            title = title,
        )
    }
}