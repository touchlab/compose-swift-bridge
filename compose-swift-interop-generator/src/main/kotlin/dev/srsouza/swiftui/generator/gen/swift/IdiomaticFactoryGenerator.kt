package dev.srsouza.swiftui.generator.gen.swift

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.gen.ViewType
import dev.srsouza.swiftui.generator.toSwift
import dev.srsouza.swiftui.generator.util.SwiftFileSpec
import dev.srsouza.swiftui.generator.util.SwiftParameterSpec
import dev.srsouza.swiftui.generator.util.SwiftPropertySpec
import dev.srsouza.swiftui.generator.util.SwiftTypeSpec
import dev.srsouza.swiftui.generator.util.Types
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier

fun buildSwiftIdiomaticFactoryFiles(
    allNativeViews: List<NativeViewInfo>
): List<SwiftFileSpec> {
    return allNativeViews.groupBy { it.factoryName }
        .map { (factoryName, nativeViews) ->
            buildSwiftIdiomaticFactory(factoryName, nativeViews)
        }
}

/**
 * Generates a Swift files that Glue the generated
 * swift factory protocol ([buildSwiftViewFactoryProtocolFiles])
 * with the generated raw factory interface from Kotlin ([buildRawFactoryPerPlatformFiles])
 * by levering the implementation behind the scenes that allows
 * the idiomatic format of [buildSwiftViewFactoryProtocolFiles].
 *
 * For each override function it consumes the Factory Swift version
 * and call it implementation and returns to Kotlin the required
 * Pair with the Delegate State and UIViewController.
 *
 * Example of code generated:
 * ```swift
 * public class iOSNativeViewFactory : ComposeNativeViewFactory {
 *
 *   private let nativeViewFactory: NativeViewFactory
 *
 *   public init(_ nativeViewFactory: NativeViewFactory) {
 *     self.nativeViewFactory = nativeViewFactory}
 *
 *   public func createMapView(coordinate: MapCoordinates, title: String) -> KotlinPair<UIViewController, MapViewDelegate> {
 *     let delegate = MapViewObservable(coordinate: coordinate, title: title)
 *     let ref = nativeViewFactory.createMapView(
 *         observable: delegate
 *     )
 *     return KotlinPair(first: ref, second: delegate)}
 * }
 * ```
 *
 * There are 3 View Types that is currently supported,
 * SwiftUI AnyView, UIViewController and UIView.
 *
 * SwiftUI View: Is wrap in a UIHostingController and we instantiate
 * the generated ObservableObject that implements the State Delegate Updater
 * allowing SwiftUI to consume states from Compose automatically.
 *
 * UIViewController and UIView: They both depend on the user to implement
 * by hand the State Delegate Updater interfaces, this can be implemented
 * in anyway that the user wants, for example by implementing directly on
 * the UIViewController or the UIView.
 */
private fun buildSwiftIdiomaticFactory(
    factoryName: String,
    nativeViews: List<NativeViewInfo>,
): SwiftFileSpec {
    val className = "iOS${Types.Members.nativeViewFactory(factoryName)}"
    val classSpec = SwiftTypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addSuperType(Types.Members.composeNativeViewFactory(factoryName).toSwift())

    for(nativeView in nativeViews) {
        val factoryFunctionName = Types.factoryFunctionName(nativeView.functionName)

        val funSpec = FunctionSpec.builder(factoryFunctionName)
            .addModifiers(Modifier.PUBLIC)

        // Adding the swift mapped parameters to the function
        nativeView.swiftParametersMapping()
            .forEach(funSpec::addParameter)

        // Defining the return type
        val interopTypeBasedOnViewType = when(nativeView.viewType) {
            ViewType.SwiftUI,
            ViewType.UIViewController -> Types.Members.uiViewController
            ViewType.UIView -> Types.Members.uiView
        }

        funSpec.returns(
            Pair::class.asClassName()
                .parameterizedBy(
                    interopTypeBasedOnViewType,
                    Types.Members.nativeViewDelegate(nativeView.functionName)
                )
                .toSwift()!!
        )

        val createFunctionName = Types.factoryFunctionName(nativeView.functionName)
        val rawParamsCode = nativeView.parameters.filterNot { it.isModifier }
            .joinToString { "${it.name}: ${it.name}" }

        when(nativeView.viewType) {
            ViewType.SwiftUI -> {
                // Instantiating ObservableObject
                funSpec.addCode("""
                    let delegate = %T($rawParamsCode)
                """.trimIndent(), Types.Members.nativeViewObservable(nativeView.functionName))
                funSpec.addCode("\n")

                // Calling the factory function from the protocol implemented
                funSpec.addCode("""
                    let ref = nativeViewFactory.$createFunctionName(
                        observable: delegate
                    )
                """.trimIndent())
                funSpec.addCode("\n")

                // returning
                funSpec.addCode("""
                    return KotlinPair(first: UIHostingController(rootView: ref), second: delegate)
                """.trimIndent(),
                )
            }
            ViewType.UIViewController,
            ViewType.UIView -> {
                // Calling the factory function from the protocol implemented
                funSpec.addCode("""
                    let (ref, delegate) = nativeViewFactory.$createFunctionName(
                        $rawParamsCode
                    )
                """.trimIndent())
                funSpec.addCode("\n")

                funSpec.addCode("""
                    return KotlinPair(first: ref, second: delegate)
                """.trimIndent(),
                )
            }
        }

        classSpec.addFunction(funSpec.build())
    }

    val initBuilder = FunctionSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
    val viewFactoryProtocolName = Types.Members.nativeViewFactory(factoryName)
    val viewFactoryParamName = "nativeViewFactory"
    val viewFactoryType = DeclaredTypeName.typeName(".$viewFactoryProtocolName")

    classSpec.addProperty(
        SwiftPropertySpec.builder(viewFactoryParamName, viewFactoryType)
            .addModifiers(Modifier.PRIVATE)
            .build()
    )

    initBuilder.addParameter(
        name = viewFactoryParamName,
        type = viewFactoryType,
        label = "_"
    )
    initBuilder.addCode("self.$viewFactoryParamName = $viewFactoryParamName")

    classSpec.addFunction(initBuilder.build())

    return SwiftFileSpec.builder(className)
        .addType(classSpec.build())
        .addImport("UIKit")
        .addImport("SwiftUI")
        .build()
}

internal fun NativeViewInfo.swiftParametersMapping(): List<SwiftParameterSpec> {
    return parameters.filterNot { it.isModifier }
        .map { param ->
            val paramSpec = SwiftParameterSpec.builder(
                parameterName = param.name,
                type = param.swiftType
            ).apply {
                if(param.swiftType is FunctionTypeName) {
                    addAttribute("escaping")
                }
            }
            paramSpec.build()
        }
}