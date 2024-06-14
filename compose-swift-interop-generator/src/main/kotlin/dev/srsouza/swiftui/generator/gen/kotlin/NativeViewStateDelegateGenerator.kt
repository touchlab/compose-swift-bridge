package dev.srsouza.swiftui.generator.gen.kotlin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.util.Types

fun buildNativeViewStateDelegateGeneratorFiles(
    allNativeViews: List<NativeViewInfo>
): List<FileSpec> {
    return allNativeViews.map { viewInfo ->
        val typeName = Types.Members.nativeViewDelegate(viewInfo.functionName)
        val interfaceSpec = TypeSpec.interfaceBuilder(typeName)

        for (param in viewInfo.parameters.filterNot { it.isModifier }) {
            interfaceSpec.addFunction(
                FunSpec.builder(
                "update${param.namePascalCase}"
            )
                .addModifiers(KModifier.ABSTRACT)
                .addParameter(ParameterSpec.builder(param.name, param.type).build())
                .build())
        }

        FileSpec.builder(typeName).addType(interfaceSpec.build())
            .build()
    }
}