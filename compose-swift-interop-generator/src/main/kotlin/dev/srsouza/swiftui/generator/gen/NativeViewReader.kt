package dev.srsouza.swiftui.generator.gen

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import dev.srsouza.swiftui.generator.toSwift
import dev.srsouza.swiftui.generator.util.SwiftTypeName
import dev.srsouza.swiftui.generator.util.Types
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toPascalCase

val errorTag = "[ComposeExpectSwift]"

data class NativeViewParameterInfo(
    val kspRef: KSValueParameter,
    val name: String,
    val type: TypeName,
    val swiftType: SwiftTypeName,
    val isModifier: Boolean,
) {
    val namePascalCase = name.toPascalCase(CaseFormat.CAMEL)
}

data class KotlinNativeViewInfo(
    val kspRef: KSFunctionDeclaration,
    val visibility: KModifier,
    val modifierParamName: String,
    val file: KSFile,
)

data class NativeViewInfo(
    val functionName: String,
    val parameters: List<NativeViewParameterInfo>,
    val factoryName: String,
    val kotlinInfo: KotlinNativeViewInfo,
    // val swiftInfo: SwiftNativeViewInfo
)

internal const val DEFAULT_FACTORY_NAME = "NativeView"

fun readNativeViewComposable(
    logger: KSPLogger,
    function: KSFunctionDeclaration
): NativeViewInfo? {
    if(function.isExpect.not()) {
        logger.error("$errorTag does not support non expect functions", function)
        return null
    }
    val file = function.containingFile
    if(file == null) {
        logger.error("$errorTag does not contain in a file", function)
        return null
    }
    val visibility = function.getVisibility().toKModifier()
    if(visibility == null) {
        logger.error("$errorTag does not conform to kotlin visibility (or it is Java Package or Local declaration)", function)
        return null
    }

    val factoryName = function.annotations
        .firstOrNull { it.shortName.getShortName() == Types.annotationName }
        ?.arguments
        ?.firstOrNull { it.name?.getShortName() == "factoryName" }
        ?.value as? String?
        ?: DEFAULT_FACTORY_NAME // In case KSP can't resolve the default value, we fallback to the default

    val functionName = function.simpleName.asString()

    val parameters = function.parameters.map { parameter ->
        val name = parameter.name?.asString()
        if (name == null) {
            logger.error("$errorTag unsupported function parameter without name", parameter)
            return null
        }

        val isModifier = parameter.type.resolve().declaration.qualifiedName
            ?.asString() == Types.modifierFqn

        val kotlinType = parameter.type.toTypeName()

        val swiftType = kotlinType.toSwift()

        if(swiftType == null) {
            logger.error("$errorTag Unable to convert Kotlin Type($kotlinType) to Swift Type", parameter)
            return null
        }

        NativeViewParameterInfo(
            kspRef = parameter,
            name = name,
            type = kotlinType,
            swiftType = swiftType,
            isModifier = isModifier,
        )
    }

    val modifierParam = parameters.firstOrNull { it.isModifier }?.name

    if(modifierParam == null) {
        logger.warn("$errorTag  does not contain a Modifier parameter", function)
        return null
    }

    return NativeViewInfo(
        functionName = functionName,
        parameters = parameters,
        factoryName = factoryName,
        kotlinInfo = KotlinNativeViewInfo(
            kspRef = function,
            visibility = visibility,
            modifierParamName = modifierParam,
            file = file
        )
    )
}