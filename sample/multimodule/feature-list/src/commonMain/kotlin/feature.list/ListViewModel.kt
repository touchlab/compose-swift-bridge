package feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import common.data.Restaurant
import common.datasource.LocalRestaurantsDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListViewModel(
    private val dataSource: LocalRestaurantsDataSource,
) : ViewModel() {
    private val mutableRestaurants: MutableStateFlow<List<Restaurant>> = MutableStateFlow(emptyList())
    val restaurants = mutableRestaurants.asStateFlow()

    init {
        viewModelScope.launch {
            val restaurants = dataSource.restaurants()
            mutableRestaurants.update { restaurants }
        }
    }
}