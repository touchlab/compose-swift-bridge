package dev.srsouza.swiftui.generator.gen.swift

import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.toSwift
import dev.srsouza.swiftui.generator.util.SwiftFileSpec
import dev.srsouza.swiftui.generator.util.SwiftParameterSpec
import dev.srsouza.swiftui.generator.util.SwiftPropertySpec
import dev.srsouza.swiftui.generator.util.SwiftTypeSpec
import dev.srsouza.swiftui.generator.util.Types
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier

fun buildNativeViewStateSwiftUIObservableObjectFiles(
    allNativeViews: List<NativeViewInfo>
): List<SwiftFileSpec> {
    return allNativeViews.map { viewInfo ->
        buildNativeViewStateSwiftUIObservableObject(viewInfo)
    }
}

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
private fun buildNativeViewStateSwiftUIObservableObject(
    viewInfo: NativeViewInfo
): SwiftFileSpec {
    val type = Types.Members.nativeViewObservable(viewInfo.functionName)
    val classSpec = SwiftTypeSpec.classBuilder(type)

    classSpec.addSuperType(Types.Members.nativeViewDelegate(viewInfo.functionName).toSwift())
    classSpec.addSuperType(Types.Members.observableObject)
    classSpec.addModifiers(Modifier.PUBLIC)

    val initBuilder = FunctionSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)

    val parameterExcludingModifier = viewInfo.parameters.filterNot { it.isModifier }
    for (param in parameterExcludingModifier) {
        classSpec.addProperty(
            SwiftPropertySpec.varBuilder(param.name, param.swiftType)
                .addAttribute("Published")
                .addModifiers(Modifier.PUBLIC)
                .build()
        )

        val paramSpec = SwiftParameterSpec.builder(
            parameterName = param.name,
            type = param.swiftType
        ).apply {
            if (param.swiftType is FunctionTypeName) {
                addAttribute("escaping")
            }
        }

        initBuilder.addParameter(paramSpec.build())

        initBuilder.addCode("self.${param.name} = ${param.name}\n")

        classSpec.addFunction(
            FunctionSpec.builder("update${param.namePascalCase}")
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