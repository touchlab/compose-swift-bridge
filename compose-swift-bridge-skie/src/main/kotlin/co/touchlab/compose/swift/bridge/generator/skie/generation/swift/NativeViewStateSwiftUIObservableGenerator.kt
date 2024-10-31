package co.touchlab.compose.swift.bridge.generator.skie.generation.swift

import co.touchlab.compose.swift.bridge.generator.skie.SwiftNativeViewInfo
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier

/**
 * Generates a handy SwiftUI ObservableObject that holds the initial
 * and current state of a Composable state parameters by
 * implementing the NativeViewStateDelegate interface
 * (@see buildNativeViewStateDelegateFiles), this way
 * SwiftUI can easily subscribe to state changes automatically.
 *
 * Example of generated code:
 * ```swift
 * public class MapViewObservable : MapViewDelegate, ObservableObject {
 *
 *   @Published
 *   public var coordinate: MapCoordinates
 *   @Published
 *   public var title: String
 *
 *   public init(coordinate: MapCoordinates, title: String) {
 *     self.coordinate = coordinate
 *     self.title = title
 *   }
 *
 *   public func updateCoordinate(coordinate: MapCoordinates) {
 *     self.coordinate = coordinate
 *   }
 *
 *   public func updateTitle(title: String) {
 *     self.title = title
 *   }
 * }
 * ```
 */
internal fun buildNativeViewStateSwiftUIObservableObject(
    viewInfo: SwiftNativeViewInfo
): SwiftFileSpec {
    val type = nativeViewObservable(viewInfo.config.viewName)
    val classSpec = SwiftTypeSpec.classBuilder(type)

    classSpec.addSuperType(viewInfo.delegateRef.primarySirClass.defaultType.evaluate().swiftPoetTypeName)
    classSpec.addSuperType(observableObject)
    classSpec.addModifiers(Modifier.PUBLIC)

    val initBuilder = FunctionSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)

    for (param in viewInfo.stateParameters) {
        val typeName = param.type.evaluate().swiftPoetTypeName
        classSpec.addProperty(
            SwiftPropertySpec.varBuilder(
                param.name,
                when(typeName) {
                    is FunctionTypeName -> {
                        typeName.copy(attributes = emptyList())
                    }
                    else -> typeName
                }
            )
                .addAttribute("Published")
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        val paramSpec = SwiftParameterSpec.builder(
            parameterName = param.name,
            type = typeName
        )

        initBuilder.addParameter(paramSpec.build())

        initBuilder.addCode("self.${param.name} = ${param.name}\n")

        classSpec.addFunction(
            FunctionSpec.builder(param.updateFunctionName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(paramSpec.build())
                .addCode("self.${param.name} = ${param.name}\n")
                .build()
        )
    }

    classSpec.addFunction(initBuilder.build())

    return SwiftFileSpec.builder(type.simpleName)
        .addType(classSpec.build())
        .build()
}