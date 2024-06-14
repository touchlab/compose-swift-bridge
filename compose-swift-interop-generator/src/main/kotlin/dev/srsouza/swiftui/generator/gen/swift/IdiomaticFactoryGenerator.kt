package dev.srsouza.swiftui.generator.gen.swift

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
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
            val className = "iOS${Types.Members.nativeViewFactory(factoryName)}"
            val classSpec = SwiftTypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperType(Types.Members.composeNativeViewFactory(factoryName).toSwift())

            for(nativeView in nativeViews) {
                val factoryFunctionName = Types.factoryFunctionName(nativeView.functionName)

                val funSpec = FunctionSpec.builder(factoryFunctionName)
                    .addModifiers(Modifier.PUBLIC)

                val parametersExcludingModifier = nativeView.parameters.filterNot { it.isModifier }
                for (param in parametersExcludingModifier) {
                    val paramSpec = SwiftParameterSpec.builder(
                        parameterName = param.name,
                        type = param.swiftType
                    ).apply {
                        if(param.swiftType is FunctionTypeName) {
                            addAttribute("escaping")
                        }
                    }
                    funSpec.addParameter(paramSpec.build())

                }

                funSpec.returns(
                    Pair::class.asClassName()
                        .parameterizedBy(
                            Types.Members.uiViewController,
                            Types.Members.nativeViewDelegate(nativeView.functionName)
                        )
                        .toSwift()!!
                )

                val createFunctionName = Types.factoryFunctionName(nativeView.functionName)
                val rawParamsCode = nativeView.parameters.filterNot { it.isModifier }
                    .joinToString { "${it.name}: ${it.name}" }

                funSpec.addCode("""
                    let delegate = %T($rawParamsCode)
                    let viewController = nativeViewFactory.$createFunctionName(
                        observable: delegate
                    )
                    return KotlinPair(first: viewController, second: delegate)
                """.trimIndent(), Types.Members.nativeViewObservable(nativeView.functionName)
                )

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

            SwiftFileSpec.builder(className)
                .addType(classSpec.build())
                .addImport("UIKit")
                .build()
        }
}