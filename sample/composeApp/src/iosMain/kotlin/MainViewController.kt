import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import dev.srsouza.compose.swift.interop.ComposeNativeViewFactory
import dev.srsouza.compose.swift.interop.LocalNativeViewFactory
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