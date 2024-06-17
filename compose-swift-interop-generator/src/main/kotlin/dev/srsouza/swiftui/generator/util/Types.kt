package dev.srsouza.swiftui.generator.util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.GenericQualifiedTypeName

object Types {
    val annotationName = "ExpectSwiftView"
    val extensionPackage = "dev.srsouza.compose.swift.interop"
    val extensionDelegatePackage = "$extensionPackage.delegate"
    val extensionAnnotation = "$extensionPackage.$annotationName"

    val modifierFqn = "androidx.compose.ui.Modifier"

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
        fun nativeViewObservable(viewName: String) = DeclaredTypeName.typeName(
            qualifiedTypeName = ".${viewName}Observable"
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
        val uiKitViewController = MemberName(
            packageName = "androidx.compose.ui.interop",
            simpleName = "UIKitViewController"
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
        val swiftUIViewController = DeclaredTypeName.typeName("UIKit.UIViewController")
        val observableObject = DeclaredTypeName.typeName("Foundation.ObservableObject")
        val published = DeclaredTypeName.typeName("Foundation.Published")
        val swiftUIAnyView = DeclaredTypeName.typeName("SwiftUI.AnyView")
    }
}
