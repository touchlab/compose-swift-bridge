package common.data

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
data class MapCoordinates(
    val lat: Double,
    val lng: Double,
) : JavaSerializable
