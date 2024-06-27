package co.touchlab.compose.expect.swift.generator.skie.generation.swift

import io.outfoxx.swiftpoet.DeclaredTypeName

typealias SwiftTypeName = io.outfoxx.swiftpoet.TypeName
typealias SwiftFileSpec = io.outfoxx.swiftpoet.FileSpec
typealias SwiftParameterSpec = io.outfoxx.swiftpoet.ParameterSpec
typealias SwiftPropertySpec = io.outfoxx.swiftpoet.PropertySpec.Companion
typealias SwiftTypeSpec = io.outfoxx.swiftpoet.TypeSpec.Companion


val swiftUIViewController = DeclaredTypeName.typeName("UIKit.UIViewController")
val swiftUIView = DeclaredTypeName.typeName("UIKit.UIView")
val observableObject = DeclaredTypeName.typeName("Foundation.ObservableObject")
val published = DeclaredTypeName.typeName("Foundation.Published")
val swiftUIAnyView = DeclaredTypeName.typeName("SwiftUI.AnyView")

fun nativeViewObservable(viewName: String) = DeclaredTypeName.typeName(
    qualifiedTypeName = ".${viewName}Observable"
)
fun nativeViewFactory(factoryName: String) = "${factoryName}Factory"
fun factoryFunctionName(functionName: String) = "create${functionName}"

val extensionPackage = "co.touchlab.compose.expect.swift"
val extensionDelegatePackage = "$extensionPackage.delegate"