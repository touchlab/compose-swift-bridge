package dev.srsouza.swiftui.generator.gen.swift

import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.gen.ViewType
import dev.srsouza.swiftui.generator.toSwift
import dev.srsouza.swiftui.generator.util.SwiftFileSpec
import dev.srsouza.swiftui.generator.util.SwiftTypeName
import dev.srsouza.swiftui.generator.util.SwiftTypeSpec
import dev.srsouza.swiftui.generator.util.Types
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TupleTypeName
import io.outfoxx.swiftpoet.TypeName

fun buildSwiftViewFactoryProtocolFiles(
    allNativeViews: List<NativeViewInfo>
): List<SwiftFileSpec> {
    return allNativeViews.groupBy { it.factoryName }
        .map { (factoryName, nativeViews) ->
            buildSwiftViewFactoryProtocol(factoryName, nativeViews)
        }
}

/**
 * Generate a Swift protocol that is idiomatic, abstracting
 * the complexity by leveraging Swift specific APIs such
 * being able to depend on a Generate swift type such as the
 * ObservableObject(@see [buildNativeViewStateSwiftUIObservableObjectFiles])
 * and SwiftUI that from Kotlin we can't use it.
 *
 * This is a sibling of [buildRawFactoryPerPlatformFiles] with
 * Swift specific features and generated code available. This
 * is the protocol that will be implemented by the user of the
 * library on Swift side.
 *
 * Example of the code generated:
 * ```swift
 * public protocol NativeViewFactory {
 *   func createMapView(observable: MapViewObservable) -> AnyView
 *   func createExampleView(param1: String, param2: String) -> (view: UIView, delegate: ExampleViewDelegate)
 * }
 * ```
 *
 * There are 3 View Types that is currently supported,
 * SwiftUI AnyView, UIViewController and UIView.
 *
 * SwiftUI: Requirement return is a AnyView and the user
 * that will implement receives a ObservableObject that
 * contain the state and it automatically update when
 * the Compose state changes.
 *
 * UIViewController and UIView: Requirement return is a
 * Swift Tuple with the View being the type (UIViewController or UIView)
 * and the State Update Delegate that the user need to implement
 * and return as well. This is due to the fact that UIKit
 * holds the state directly in the View, similar to how
 * Android View System works.
 */
private fun buildSwiftViewFactoryProtocol(
    factoryName: String,
    nativeViews: List<NativeViewInfo>,
): SwiftFileSpec {
    val protocolName = Types.Members.nativeViewFactory(factoryName)
    val protocolSpec = SwiftTypeSpec.protocolBuilder(protocolName)
        .addModifiers(Modifier.PUBLIC)

    for(viewInfo in nativeViews) {
        val factoryFunctionName = Types.factoryFunctionName(viewInfo.functionName)

        val funSpec = FunctionSpec.abstractBuilder(factoryFunctionName)

        parametersBasedOnViewType(viewInfo)
            .forEach(funSpec::addParameter)

        funSpec.returns(returnTypeBasedOnViewType(viewInfo))

        protocolSpec.addFunction(funSpec.build())
    }

    return SwiftFileSpec.builder(protocolName)
        .addType(protocolSpec.build())
        .build()
}

private fun parametersBasedOnViewType(viewInfo: NativeViewInfo): List<ParameterSpec> {
    return when(viewInfo.viewType) {
        ViewType.SwiftUI -> {
            val param = ParameterSpec.builder(
                parameterName = "observable",
                type = Types.Members.nativeViewObservable(viewInfo.functionName)
            )
                .build()

            listOf(param)
        }
        ViewType.UIViewController,
        ViewType.UIView -> {
            viewInfo.swiftParametersMapping()
        }
    }
}

private fun returnTypeBasedOnViewType(viewInfo: NativeViewInfo): SwiftTypeName {
    // Utility function to build Tuple with view and delegate
    // similar to Kotlin Pair, this is only used with UiKit
    // because SwiftUI we have a ObservableObject
    fun uikitReturnType(type: DeclaredTypeName): TupleTypeName {
        val nativeDelegate =
            Types.Members.nativeViewDelegate(viewInfo.functionName).toSwift()
        return TupleTypeName.of(
            "view" to type,
            "delegate" to nativeDelegate,
        )
    }

    return when(viewInfo.viewType) {
        ViewType.SwiftUI -> {
            Types.Members.swiftUIAnyView
        }
        ViewType.UIViewController -> {
            uikitReturnType(Types.Members.swiftUIViewController)
        }
        ViewType.UIView -> {
            uikitReturnType(Types.Members.swiftUIView)
        }
    }
}