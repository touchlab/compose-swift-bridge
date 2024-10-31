package co.touchlab.compose.swift.bridge.generator.ksp.gen

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toPascalCase

val errorTag = "[ComposeExpectSwift]"

data class NativeViewParameterInfo(
    val kspRef: KSValueParameter,
    val name: String,
    val type: TypeName,
    val isModifier: Boolean,
) {
    val namePascalCase = name.toPascalCase(CaseFormat.CAMEL)
}

data class KotlinNativeViewInfo(
    val kspRef: KSFunctionDeclaration,
    val visibility: KModifier,
    val modifierParamName: String?,
    val file: KSFile,
)

data class NativeViewInfo(
    val functionName: String,
    val parameters: List<NativeViewParameterInfo>,
    val factoryName: String,
    val viewType: ViewType,
    val keepStateCrossNavigation: Boolean,
    val renderComposableFqn: String,
    val kotlinInfo: KotlinNativeViewInfo,
)

enum class ViewType {
    SwiftUI,
    UIViewController,
    UIView,
}

internal const val DEFAULT_FACTORY_NAME = "NativeView"
internal const val DEFAULT_VIEWCONTROLLER_INTEROP_COMPOSABLE = "androidx.compose.ui.interop.UIKitViewController"
internal const val DEFAULT_UIKITVIEW_INTEROP_COMPOSABLE = "androidx.compose.ui.interop.UIKitView"
internal const val DEFAULT_KEEP_STATE_CROSS_NAVIGATION = false
internal val DEFAULT_VIEW_TYPE = ViewType.SwiftUI

fun readNativeViewComposable(
    defaultFactoryName: String,
    defaultViewControllerInteropComposableFqn: String,
    defaultUiKitViewInteropComposableFqn: String,
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

    val expectSwift = function.annotations
        .firstOrNull { it.shortName.getShortName() == Types.annotationName }

    val factoryName = expectSwift
        ?.arguments
        ?.firstOrNull { it.name?.getShortName() == "factoryName" }
        ?.value as? String?
        ?: defaultFactoryName

    val viewType = (expectSwift
        ?.arguments
        ?.firstOrNull { it.name?.getShortName() == "type" }
        ?.value as? KSType?)
        ?.declaration?.simpleName?.asString()
        ?.let { enumValueName ->
            ViewType.entries.find { it.name == enumValueName }
        }
        ?: DEFAULT_VIEW_TYPE // In case KSP can't resolve the default value, we fallback to the default

    val keepStateCrossNavigation = expectSwift
        ?.arguments
        ?.firstOrNull { it.name?.getShortName() == "keepStateCrossNavigation" }
        ?.value as? Boolean?
        ?: DEFAULT_KEEP_STATE_CROSS_NAVIGATION // In case KSP can't resolve the default value, we fallback to the default

    val customComposableFunction = function.annotations
        .firstOrNull { it.shortName.getShortName() == Types.customInteropComposableAnnotationName }

    val customComposableFunctionFqn = (customComposableFunction
        ?.arguments
        ?.firstOrNull { it.name?.getShortName() == "composableFqn" }
        ?.value as? String?)
        ?.takeIf { it.isNotBlank() }

    val renderComposableFqn = customComposableFunctionFqn
        ?: when(viewType) {
            ViewType.SwiftUI,
            ViewType.UIViewController -> defaultViewControllerInteropComposableFqn
            ViewType.UIView -> defaultUiKitViewInteropComposableFqn
        }

    val functionName = function.simpleName.asString()

    val parameters = function.parameters.map { parameter ->
        val name = parameter.name?.asString()
        if (name == null) {
            logger.error("$errorTag unsupported function parameter without name", parameter)
            return null
        }

        val isModifier = parameter.type.resolve().declaration.qualifiedName
            ?.asString() == Types.Members.modifier.canonicalName

        val kotlinType = parameter.type.toTypeName()

        NativeViewParameterInfo(
            kspRef = parameter,
            name = name,
            type = kotlinType,
            isModifier = isModifier,
        )
    }

    val modifierParam = parameters.firstOrNull { it.isModifier }?.name

    if(modifierParam == null) {
        logger.warn("$errorTag is recommend to have a Modifier parameter in @ExpectSwiftView functions", function)
    }

    return NativeViewInfo(
        functionName = functionName,
        parameters = parameters,
        factoryName = factoryName,
        viewType = viewType,
        keepStateCrossNavigation = keepStateCrossNavigation,
        renderComposableFqn = renderComposableFqn,
        kotlinInfo = KotlinNativeViewInfo(
            kspRef = function,
            visibility = visibility,
            modifierParamName = modifierParam,
            file = file
        )
    )
}