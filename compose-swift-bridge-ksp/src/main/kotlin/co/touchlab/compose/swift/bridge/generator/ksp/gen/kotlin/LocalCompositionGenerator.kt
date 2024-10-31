package co.touchlab.compose.swift.bridge.generator.ksp.gen.kotlin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types.Members.composeNativeViewFactory
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types.Members.compositionLocalOf
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types.Members.localNativeViewFactory
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types.Members.providableCompositionLocal
import co.touchlab.compose.swift.bridge.generator.ksp.gen.NativeViewInfo

/**
 * Generates all Local Composition for the all distinct
 * @ExpectSwiftView(factoryName = ?) factoryName defined
 * in the annotation.
 *
 * This is generated in the Common main, the main reason
 * is for being able to use it on the `iosMain`, currently
 * KSP does not allow generating code for "common" source sets
 * besides commonMain.
 *
 * The final generation looks like
 *
 * ```kotlin
 * public val LocalNativeViewFactory: ProvidableCompositionLocal<ComposeNativeViewFactory> =
 *     compositionLocalOf { error("""You have to provide LocalNativeViewFactory""") }
 * ```
 */
fun buildLocalCompositionFile(
    collectedNativeViews: List<NativeViewInfo>
): FileSpec.Builder {
    val fileSpec = FileSpec.builder(
        packageName = Types.extensionPackage,
        fileName = "Compositions"
    )

    for ((factoryName, _) in collectedNativeViews.groupBy { it.factoryName }) {
        val localCompositionName = localNativeViewFactory(factoryName).simpleName
        val localCompositionType = providableCompositionLocal
            .parameterizedBy(composeNativeViewFactory(factoryName))

        val propSpec = PropertySpec.builder(
            name = localCompositionName,
            type = localCompositionType,
        ).initializer(
            "%M(defaultFactory = { error(\"\"\"You have to provide ${localCompositionName}\"\"\") })",
            compositionLocalOf
        ).build()

        fileSpec.addProperty(propSpec)
    }

    return fileSpec
}