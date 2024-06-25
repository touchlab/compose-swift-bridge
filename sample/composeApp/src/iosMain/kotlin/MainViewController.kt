import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.compose.expect.swift.ComposeNativeViewFactory
import co.touchlab.compose.expect.swift.LocalNativeViewFactory
import platform.UIKit.UIViewController
import ui.AppScreen

fun MainViewController(
    generatedViewFactory: ComposeNativeViewFactory
): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(
        LocalNativeViewFactory provides generatedViewFactory,
    ) {
        AppScreen()
    }
}