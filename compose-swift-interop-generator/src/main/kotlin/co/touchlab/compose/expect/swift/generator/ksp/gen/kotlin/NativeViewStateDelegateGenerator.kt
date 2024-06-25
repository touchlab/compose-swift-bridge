package co.touchlab.compose.expect.swift.generator.ksp.gen.kotlin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import co.touchlab.compose.expect.swift.generator.ksp.gen.NativeViewInfo
import co.touchlab.compose.expect.swift.generator.ksp.util.Types

fun buildNativeViewStateDelegateFiles(
    allNativeViews: List<NativeViewInfo>
): List<FileSpec> {
    return allNativeViews.map { viewInfo ->
        buildNativeViewStateDelegate(viewInfo)
    }
}

/**
 * Generate a interface with update functions for a NativeView
 * containing all parameters from the Composable that
 * when the state change (aka `remember(parameter)`) is
 * called.
 *
 * Example output
 * ```kotlin
 * public interface MapViewDelegate {
 *   public fun updateCoordinate(coordinate: MapCoordinates)
 *
 *   public fun updateTitle(title: String)
 * }
 * ```
 */
private fun buildNativeViewStateDelegate(viewInfo: NativeViewInfo): FileSpec {
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

    return FileSpec.builder(typeName).addType(interfaceSpec.build())
        .build()
}