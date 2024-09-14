package feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import common.data.Restaurant
import feature.detail.components.MapView

data class DetailScreen(
    private val restaurant: Restaurant
) : Screen {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Label(restaurant.category)

            Spacer(Modifier.height(20.dp))

            Description(restaurant)

            Spacer(Modifier.height(12.dp))

            Address(restaurant)

            Spacer(Modifier.height(12.dp))

            MapView(
                modifier = Modifier.fillMaxWidth()
                    .height(200.dp),
                placeName = restaurant.name,
                coordinate = restaurant.location,
            )
        }
    }
}

@Composable
private fun Description(
    restaurant: Restaurant
) {
    ContentContainer(
        title = "Description"
    ) {
        Text(
            text = restaurant.description,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun Address(
    restaurant: Restaurant
) {
    ContentContainer(
        title = "Address",
    ) {
        Column {
            Row {
                Icon(
                    Icons.Filled.Place,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = restaurant.address,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text("Denmark")
                }
            }
        }
    }
}

@Composable
private fun ContentContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
                .padding(8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun Label(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                top = 4.dp,
                bottom = 4.dp,
                start = 8.dp,
                end = 8.dp
            ),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
