package navigation

import cafe.adriel.voyager.core.registry.ScreenProvider
import common.data.Restaurant

sealed class SharedScreen : ScreenProvider {
    object List : SharedScreen()
    data class Detail(val restaurant: Restaurant) : SharedScreen()
}