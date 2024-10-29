package sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import platform.UIKit.UIViewController

@Composable
fun ViewControllerCustom(
    modifier: Modifier,
    factory: () -> UIViewController
) {
    UIKitViewController(
        modifier = modifier,
        factory = factory,
        properties = UIKitInteropProperties(isInteractive = false, isNativeAccessibilityEnabled = true),
    )
}