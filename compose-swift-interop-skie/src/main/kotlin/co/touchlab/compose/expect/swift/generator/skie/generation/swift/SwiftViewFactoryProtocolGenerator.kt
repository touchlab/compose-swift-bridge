package co.touchlab.compose.expect.swift.generator.skie.generation.swift

import co.touchlab.compose.expect.swift.ViewType
import co.touchlab.compose.expect.swift.generator.skie.SwiftNativeViewInfo
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.TupleTypeName

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
internal fun buildSwiftViewFactoryProtocol(
    factoryName: String,
    nativeViews: List<SwiftNativeViewInfo>,
): SwiftFileSpec {
    val protocolName = nativeViewFactory(factoryName)
    val protocolSpec = SwiftTypeSpec.protocolBuilder(protocolName)
        .addModifiers(Modifier.PUBLIC)

    for(viewInfo in nativeViews) {
        val factoryFunctionName = factoryFunctionName(viewInfo.config.viewName)

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

private fun parametersBasedOnViewType(viewInfo: SwiftNativeViewInfo): List<ParameterSpec> {
    return when(viewInfo.config.viewType) {
        ViewType.SwiftUI -> {
            val param = ParameterSpec.builder(
                parameterName = "observable",
                type = nativeViewObservable(viewInfo.config.viewName)
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

private fun returnTypeBasedOnViewType(viewInfo: SwiftNativeViewInfo): SwiftTypeName {
    // Utility function to build Tuple with view and delegate
    // similar to Kotlin Pair, this is only used with UiKit
    // because SwiftUI we have a ObservableObject
    fun uikitReturnType(type: DeclaredTypeName): TupleTypeName {
        val nativeDelegate = viewInfo.delegateRef.originalSirClass.toType().evaluate().swiftPoetTypeName
        return TupleTypeName.of(
            "view" to type,
            "delegate" to nativeDelegate,
        )
    }

    return when(viewInfo.config.viewType) {
        ViewType.SwiftUI -> {
            swiftUIAnyView
        }
        ViewType.UIViewController -> {
            uikitReturnType(swiftUIViewController)
        }
        ViewType.UIView -> {
            uikitReturnType(swiftUIView)
        }
    }
}