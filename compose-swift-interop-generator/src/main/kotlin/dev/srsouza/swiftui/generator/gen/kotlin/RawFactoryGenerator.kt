package dev.srsouza.swiftui.generator.gen.kotlin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import dev.srsouza.swiftui.generator.GeneratorTarget
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.util.Types

fun buildRawFactoryPerPlatformFiles(
    allNativeViews: List<NativeViewInfo>,
    target: GeneratorTarget,
): List<FileSpec> {
    val generatedFiles = allNativeViews.groupBy { it.factoryName }
        .map { (factoryName, nativeViews)->
            val interfaceType = Types.Members.composeNativeViewFactory(factoryName)
            val interfaceSpec = TypeSpec.interfaceBuilder(interfaceType)

            when(target) {
                is GeneratorTarget.Common -> {
                    // generate expect actual
                    interfaceSpec.addModifiers(KModifier.EXPECT)
                }
                is GeneratorTarget.IOS -> {
                    // generate actual for ios target with all factory methods
                    interfaceSpec.addModifiers(KModifier.ACTUAL)
                    for (viewInfo in nativeViews) {
                        val factoryFunctionName = Types.factoryFunctionName(viewInfo.functionName)
                        val factoryFunctionParameters =
                            viewInfo.parameters.filterNot { it.isModifier }

                        val funSpec = FunSpec.builder(factoryFunctionName)
                            .addModifiers(KModifier.ABSTRACT)

                        for (param in factoryFunctionParameters) {
                            funSpec.addParameter(
                                name = param.name,
                                type = param.type
                            )
                        }

                        funSpec.returns(
                            Pair::class.asClassName().parameterizedBy(
                                Types.Members.uiViewController,
                                Types.Members.nativeViewDelegate(viewInfo.functionName)
                            )
                        )

                        interfaceSpec.addFunction(funSpec.build())
                    }
                }

                is GeneratorTarget.NonIOS -> {
                    // generate empty actual for non ios platforms
                    interfaceSpec.addModifiers(KModifier.ACTUAL)
                }
            }

            FileSpec.builder(
                packageName = interfaceType.packageName,
                fileName = "${interfaceType.simpleName}${target.suffix}"
            )
                .addType(interfaceSpec.build())
                .build()
        }

    return generatedFiles
}