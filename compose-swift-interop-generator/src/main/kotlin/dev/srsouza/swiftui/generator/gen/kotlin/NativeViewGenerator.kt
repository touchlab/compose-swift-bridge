package dev.srsouza.swiftui.generator.gen.kotlin

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.srsouza.swiftui.generator.util.Types
import dev.srsouza.swiftui.generator.util.Types.Members.localNativeViewFactory
import dev.srsouza.swiftui.generator.util.Types.Members.nativeViewHolderViewModel
import dev.srsouza.swiftui.generator.util.Types.Members.random
import dev.srsouza.swiftui.generator.util.Types.Members.remember
import dev.srsouza.swiftui.generator.util.Types.Members.rememberSaveable
import dev.srsouza.swiftui.generator.util.Types.Members.uiKitViewController
import dev.srsouza.swiftui.generator.util.Types.Members.viewModelComposable
import dev.srsouza.swiftui.generator.util.Types.factoryFunctionName
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.getKModifiers
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toPascalCase


/**
 * Creates all the File specs for all Native View annotated with
 * @ExpectSwiftView discovered in the current round of KSP.
 *
 * It composes all NativeViews that are in the same Kotlin file and
 * generate the actual function.
 *
 * @see [buildNativeViewActual] to more details on how Actual function is generated.
 */
fun buildNativeViewsActualImplementationFiles(
    nativeViews: List<NativeViewInfo>,
    targetName: String,
): List<FileSpec> {
    val nativeViewFiles = mutableMapOf<String, FileSpec.Builder>()

    for (viewInfo in nativeViews) {
        val fileSpec = nativeViewFiles.getOrPut(viewInfo.kotlinInfo.file.filePath) {
            FileSpec.builder(
                fileName = "${viewInfo.kotlinInfo.file.fileName.removeSuffix(".kt")}.$targetName",
                packageName = viewInfo.kotlinInfo.file.packageName.asString(),
            )
        }

        val actualFunSpec = buildNativeViewActual(viewInfo)

        fileSpec.addFunction(actualFunSpec)
    }

    return nativeViewFiles.values.map { it.build() }
}

/**
 * Builds the Native View actual function for
 * expect functions annotated with `@ExpectSwiftView`.
 *
 * ```kotlin
 * @ExpectSwiftView
 * @Composable
 * expect fun MapView(
 *     modifier: Modifier = Modifier,
 *     coordinate: MapCoordinates,
 *     title: String,
 * )
 * ```
 *
 * The generated function looks like this:
 *
 * ```kotlin
 * @Composable
 * public actual fun MapView(
 *   modifier: Modifier,
 *   coordinate: MapCoordinates,
 *   title: String,
 * ) {
 *   val factory = LocalNativeViewFactory.current
 *
 *   val key = rememberSaveable { Random.nextInt().toString(16) }
 *
 *   val viewModel = viewModel(key = key) {
 *       NativeViewHolderViewModel(
 *           { factory.createMapView(coordinate, title) }
 *       )
 *   }
 *   val delegate = remember(viewModel) { viewModel.delegate }
 *   remember(coordinate) { delegate.updateCoordinate(coordinate) }
 *   remember(title) { delegate.updateTitle(title) }
 *   UIKitViewController(
 *       modifier = modifier,
 *       factory = { viewModel.viewController },
 *       update = { },
 *   )
 * }
 * ```
 */
fun buildNativeViewActual(
    viewInfo: NativeViewInfo,
): FunSpec {
    val funSpec = FunSpec.builder(
        name = viewInfo.functionName,
    )

    val experimentalForeignApiClass = ClassName(
        packageName = "kotlinx.cinterop",
        "ExperimentalForeignApi"
    )
    funSpec.addAnnotation(
        AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
            .addMember("%T::class", experimentalForeignApiClass)
            .build()
    )

    funSpec.addAnnotations(
        viewInfo.kotlinInfo.kspRef.annotations
            .filterNot { it.shortName.asString() == "ExpectSwiftView" }
            .map { it.toAnnotationSpec() }
            .toList()
    )
    funSpec.addModifiers(KModifier.ACTUAL, viewInfo.kotlinInfo.visibility)

    for(parameter in viewInfo.parameters) {
        funSpec.addParameter(
            ParameterSpec.builder(
                name = parameter.name,
                type = parameter.type,
                modifiers = parameter.kspRef.getKModifiers(),
            )
                .build()
        )
    }

    val factoryFunctionName = factoryFunctionName(viewInfo.functionName)
    val factoryFunctionParameters = viewInfo.parameters.filterNot { it.isModifier }
        .joinToString { it.name }

    funSpec.addCode(
        """
            val factory = %M.current
            
            val key = %M { %M.nextInt().toString(16) }

            val viewModel = %M(key = key) {
                %M(
                    { factory.$factoryFunctionName($factoryFunctionParameters) }
                )
            }
            val delegate = %M(viewModel) { viewModel.delegate }
        """.trimIndent(),
        localNativeViewFactory(viewInfo.factoryName),
        rememberSaveable,
        random,
        viewModelComposable,
        nativeViewHolderViewModel,
        remember
    )

    for (parameter in viewInfo.parameters) {
        if(parameter.isModifier) continue // ignore Modifier parameters

        val namePascalCase = parameter.name.toPascalCase(CaseFormat.CAMEL)
        funSpec.addCode("\n")
        funSpec.addCode("%M(${parameter.name}) { delegate.update$namePascalCase(${parameter.name}) }", remember)
    }

    funSpec.addCode("\n")
    funSpec.addCode("""
        %M(
            modifier = ${viewInfo.kotlinInfo.modifierParamName},
            factory = { viewModel.viewController },
            update = { },
        )
    """.trimIndent(), uiKitViewController)

    return funSpec.build()
}