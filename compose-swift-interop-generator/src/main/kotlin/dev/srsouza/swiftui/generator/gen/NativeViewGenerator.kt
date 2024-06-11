package dev.srsouza.swiftui.generator.gen

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.srsouza.swiftui.generator.Helpers
import dev.srsouza.swiftui.generator.Helpers.Members.launchedEffect
import dev.srsouza.swiftui.generator.Helpers.Members.localNativeViewFactory
import dev.srsouza.swiftui.generator.Helpers.Members.nativeViewHolderViewModel
import dev.srsouza.swiftui.generator.Helpers.Members.random
import dev.srsouza.swiftui.generator.Helpers.Members.remember
import dev.srsouza.swiftui.generator.Helpers.Members.rememberSaveable
import dev.srsouza.swiftui.generator.Helpers.Members.uiKitViewController
import dev.srsouza.swiftui.generator.Helpers.Members.viewModelComposable
import dev.srsouza.swiftui.generator.Helpers.factoryFunctionName
import dev.srsouza.swiftui.generator.NativeView
import dev.srsouza.swiftui.generator.NativeViewParameter
import dev.srsouza.swiftui.generator.getKModifiers
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toPascalCase

/**
 * Builds the Native View factory actual function for
 * expect functions annotated with `@ExpectSwiftView` and return
 * NativeView information for next generators or return null.
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
fun buildNativeViewOrNull(
    logger: KSPLogger,
    function: KSFunctionDeclaration
): NativeView? {
    if(function.isExpect.not()) {
        logger.warn("[SwiftGen] ${function.simpleName.asString()} is not expect fun")
        return null
    }
    val file = function.containingFile
    if(file == null) {
        logger.warn("[SwiftGen] ${function.simpleName.asString()} does not contain file")
        return null
    }
    val visibility = function.getVisibility().toKModifier()
    if(visibility == null) {
        logger.warn("[SwiftGen] ${function.simpleName.asString()} does contain a explicit kotlin visibility")
        return null
    }

    val factoryName = function.annotations
        .firstOrNull { it.shortName.getShortName() == Helpers.annotationName }
        ?.arguments
        ?.firstOrNull { it.name?.getShortName() == "factoryName" }
        ?.value as? String?
        ?: "NativeView" // In case KSP can't resolve the default value, we fallback to the default

    val functionName = function.simpleName.asString()

    val funSpec = FunSpec.builder(
        name = functionName,
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
        function.annotations
            .filterNot { it.shortName.asString() == "ExpectSwiftView" }
            .map { it.toAnnotationSpec() }
            .toList()
    )
    funSpec.addModifiers(KModifier.ACTUAL, visibility)

    val parameters = mutableListOf<NativeViewParameter>()

    for(parameter in function.parameters) {
        val name = parameter.name?.asString()
        if(name == null) {
            logger.warn("[SwiftGen] ${function.simpleName.asString()} unsupported function parameter without name")
            return null
        }

        val isModifier = parameter.type.resolve().declaration.qualifiedName
            ?.asString() == "androidx.compose.ui.Modifier"

        funSpec.addParameter(
            ParameterSpec.builder(
                name = name,
                type = parameter.type.toTypeName(),
                modifiers = parameter.getKModifiers(),
            )
                .build()
        )

        parameters += NativeViewParameter(
            name = name,
            type = parameter.type.toTypeName(),
            isModifier = isModifier,
        )
    }

    val factoryFunctionName = factoryFunctionName(functionName)
    val factoryFunctionParameters = parameters.filterNot { it.isModifier }
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
        localNativeViewFactory(factoryName),
        rememberSaveable,
        random,
        viewModelComposable,
        nativeViewHolderViewModel,
        remember
    )

    for ((paramName, _, isModifier) in parameters) {
        if(isModifier) continue // ignore Modifier parameters

        val namePascalCase = paramName.toPascalCase(CaseFormat.CAMEL)
        funSpec.addCode("\n")
        funSpec.addCode("%M($paramName) { delegate.update$namePascalCase($paramName) }", remember)
    }

    val modifierParam = parameters.firstOrNull { it.isModifier }?.name

    if(modifierParam == null) {
        logger.warn("[SwiftGen] ${function.simpleName.asString()} does not contain a Modifier param")
        return null
    }

    funSpec.addCode("\n")
    funSpec.addCode("""
        %M(
            modifier = $modifierParam,
            factory = { viewModel.viewController },
            update = { },
        )
    """.trimIndent(), uiKitViewController)

    return NativeView(
        name = functionName,
        parameters = parameters,
        factoryName = factoryName,
        actualFunSpec = funSpec.build(),
        file = file,
    )
}