package co.touchlab.compose.swift.bridge.generator.skie.generation.swift

import co.touchlab.compose.swift.bridge.ViewType
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import co.touchlab.compose.swift.bridge.generator.skie.SwiftNativeViewInfo
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.parameterizedBy

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
internal fun buildSwiftIdiomaticFactory(
    factoryName: String,
    nativeViews: List<SwiftNativeViewInfo>,
    composeNativeViewFactory: KirClass,
    kotlinPair: SirType,
): SwiftFileSpec {
    val className = iOSNativeViewFactory(factoryName)
    val classSpec = SwiftTypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addSuperType(
            composeNativeViewFactory.originalSirClass.toType().evaluate().swiftPoetTypeName
        )

    for(viewInfo in nativeViews) {
        val kotlinFactoryFunction = composeNativeViewFactory.callableDeclarations
            .filterIsInstance<KirSimpleFunction>()
            .first { it.kotlinName == factoryFunctionName(viewInfo.config.viewName) }

        val factoryFunctionName = factoryFunctionName(viewInfo.config.viewName)

        val funSpec = FunctionSpec.builder(factoryFunctionName)
            .addModifiers(Modifier.PUBLIC)

        // Adding the swift mapped parameters to the function
        kotlinFactoryFunction.valueParameters.forEach { param ->
            val swiftParam = param.originalSirValueParameter!!
            funSpec.addParameter(
                SwiftParameterSpec.builder(
                    parameterName = swiftParam.name,
                    type = swiftParam.type.evaluate().swiftPoetTypeName
                ).build()
            )
        }

        // Defining the return type
        val interopTypeBasedOnViewType = when(viewInfo.config.viewType) {
            ViewType.SwiftUI,
            ViewType.UIViewController -> swiftUIViewController
            ViewType.UIView -> swiftUIView
        }

        funSpec.returns(
            DeclaredTypeName.typeName(kotlinPair.evaluate().swiftPoetTypeName.name)
                .parameterizedBy(
                    interopTypeBasedOnViewType,
                    viewInfo.delegateRef.originalSirClass.toType().evaluate().swiftPoetTypeName
                )
        )

        val createFunctionName = factoryFunctionName(viewInfo.config.viewName)
        val rawParamsCode = viewInfo.stateParameters
            .joinToString { "${it.name}: ${it.name}" }

        when(viewInfo.config.viewType) {
            ViewType.SwiftUI -> {
                // Instantiating ObservableObject
                funSpec.addCode("""
                    let delegate = %T($rawParamsCode)
                """.trimIndent(), nativeViewObservable(viewInfo.config.viewName)
                )
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
    val viewFactoryProtocolName = nativeViewFactory(factoryName)
    val viewFactoryParamName = iOSNativeViewFactoryInitParameterName
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

internal fun SwiftNativeViewInfo.swiftParametersMapping(): List<SwiftParameterSpec> {
    return stateParameters
        .map { param ->
            val paramSpec = SwiftParameterSpec.builder(
                parameterName = param.name,
                type = param.type.evaluate().swiftPoetTypeName
            )
            paramSpec.build()
        }
}