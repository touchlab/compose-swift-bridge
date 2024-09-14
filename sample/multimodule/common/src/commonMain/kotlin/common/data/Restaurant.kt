package common.data

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
data class Restaurant(
    val name: String,
    val category: String,
    val address: String,
    val location: MapCoordinates,
    val description: String
) : JavaSerializable
