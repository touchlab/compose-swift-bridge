package dev.srsouza.swiftui.generator.gen.swift

import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.gen.ViewType
import dev.srsouza.swiftui.generator.util.SwiftFileSpec
import dev.srsouza.swiftui.generator.util.SwiftTypeSpec
import dev.srsouza.swiftui.generator.util.Types
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

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
 *   func createMapView(observable: MapViewObservable) -> UIViewController
 * }
 * ```
 *
 * There are two View Types that is currently supported,
 * SwiftUI AnyView and UIViewController.
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

        // TODO: allow disable Observable generation
        funSpec.addParameter(
            name = "observable",
            type = Types.Members.nativeViewObservable(viewInfo.functionName)
        )

        when(viewInfo.viewType) {
            ViewType.SwiftUI -> {
                funSpec.returns(Types.Members.swiftUIAnyView)
            }
            ViewType.UIViewController -> {
                funSpec.returns(Types.Members.swiftUIViewController)
            }
        }

        protocolSpec.addFunction(funSpec.build())
    }

    return SwiftFileSpec.builder(protocolName)
        .addType(protocolSpec.build())
        .build()
}