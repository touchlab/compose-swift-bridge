package co.touchlab.compose.swift.bridge.generator.ksp.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

object Types {
    val annotationName = "ExpectSwiftView"
    val customInteropComposableAnnotationName = "ExpectSwiftViewCustomInteropComposable"
    val extensionPackage = "co.touchlab.compose.swift.bridge"
    val extensionDelegatePackage = "$extensionPackage.delegate"
    val extensionAnnotation = "$extensionPackage.$annotationName"

    fun factoryFunctionName(functionName: String) = "create${functionName}"

    object Members {
        fun composeNativeViewFactory(factoryName: String) = ClassName(
            packageName = extensionPackage,
            "Compose${nativeViewFactory(factoryName)}"
        )
        fun localNativeViewFactory(factoryName: String) = MemberName(
            packageName = extensionPackage,
            simpleName = "Local${nativeViewFactory(factoryName)}"
        )
        fun nativeViewFactory(factoryName: String) = "${factoryName}Factory"
        fun nativeViewDelegate(viewName: String) = ClassName(
            packageName = extensionDelegatePackage,
            "${viewName}Delegate"
        )

        val generatedCodeMarker = ClassName(
            extensionPackage,
            "GeneratedExpectSwiftView"
        )
        val viewModelComposable = MemberName(
            packageName = "androidx.lifecycle.viewmodel.compose",
            simpleName = "viewModel"
        )
        val nativeViewHolderViewModel = MemberName(
            packageName = extensionPackage,
            simpleName = "NativeViewHolderViewModel"
        )
        val rememberSaveable = MemberName(
            packageName = "androidx.compose.runtime.saveable",
            simpleName = "rememberSaveable"
        )
        val remember = MemberName(
            packageName = "androidx.compose.runtime",
            simpleName = "remember"
        )
        val launchedEffect = MemberName(
            packageName = "androidx.compose.runtime",
            simpleName = "LaunchedEffect"
        )
        val random = MemberName(
            packageName = "kotlin.random",
            simpleName = "Random"
        )
        val providableCompositionLocal = ClassName(
            packageName = "androidx.compose.runtime",
            "ProvidableCompositionLocal"
        )
        val compositionLocalOf = MemberName(
            packageName = "androidx.compose.runtime",
            "compositionLocalOf"
        )
        val uiViewController = ClassName(
            packageName = "platform.UIKit",
            "UIViewController"
        )
        val uiView = ClassName(
            packageName = "platform.UIKit",
            "UIView"
        )
        val modifier = ClassName(
            packageName = "androidx.compose.ui",
            "Modifier"
        )
    }
}
