package application

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.registry.ScreenRegistry
import cafe.adriel.voyager.jetpack.ProvideNavigatorLifecycleKMPSupport
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import co.touchlab.compose.swift.bridge.DetailFactory
import co.touchlab.compose.swift.bridge.ListFactory
import co.touchlab.compose.swift.bridge.LocalDetailFactory
import co.touchlab.compose.swift.bridge.LocalListFactory
import feature.detail.DetailScreen
import feature.list.ListScreen
import navigation.SharedScreen
import platform.UIKit.UIViewController

fun MainComposableViewController(
    listFeatureNativeViews: ListFactory,
    detailFeatureNativeViews: DetailFactory,
): UIViewController {
    ScreenRegistry {
        register<SharedScreen.List> {
            ListScreen()
        }
        register<SharedScreen.Detail> {
            DetailScreen(restaurant = it.restaurant)
        }
    }

    return ComposeUIViewController {
        Application(
            listFeatureNativeViews = listFeatureNativeViews,
            detailFeatureNativeViews = detailFeatureNativeViews,
        )
    }
}

@OptIn(ExperimentalVoyagerApi::class)
@Composable
private fun Application(
    listFeatureNativeViews: ListFactory,
    detailFeatureNativeViews: DetailFactory,
) {
    CompositionLocalProvider(
        LocalListFactory provides listFeatureNativeViews,
        LocalDetailFactory provides detailFeatureNativeViews,
    ) {
        ProvideNavigatorLifecycleKMPSupport {
            Navigator(ListScreen()) { navigator ->
                Scaffold(
                    topBar = {
                        TopBar(navigator)
                    },
                    content = { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            CurrentScreen()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navigator: Navigator) {
    CenterAlignedTopAppBar(
        title = {
            HeaderText()
        },
        navigationIcon = {
            AnimatedVisibility(
                navigator.size > 1,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                IconButton(
                    onClick = { navigator.pop() },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                    )
                }
            }
        }
    )
}

@Composable
internal fun HeaderText(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
            )
    ) {
        Text(
            text = "Restaurants",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(4.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
