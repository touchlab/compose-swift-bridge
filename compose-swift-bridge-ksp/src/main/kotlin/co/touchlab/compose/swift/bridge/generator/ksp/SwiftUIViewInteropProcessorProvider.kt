package co.touchlab.compose.swift.bridge.generator.ksp

import co.touchlab.compose.swift.bridge.generator.ksp.gen.DEFAULT_FACTORY_NAME
import co.touchlab.compose.swift.bridge.generator.ksp.gen.DEFAULT_UIKITVIEW_INTEROP_COMPOSABLE
import co.touchlab.compose.swift.bridge.generator.ksp.gen.DEFAULT_VIEWCONTROLLER_INTEROP_COMPOSABLE
import com.google.devtools.ksp.processing.NativePlatformInfo
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

enum class GeneratorTarget(val fileSuffix: String) {
    IOS(".ios"),
    COMMON(""),
    NON_IOS(".nonios")
}

internal class SwiftUIViewInteropProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SwiftUIViewInteropProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            target = generatorTargetPlatform(environment),
            defaultFactoryName = environment.options["compose-swift-bridge.defaultFactoryName"] ?: DEFAULT_FACTORY_NAME,
            defaultViewControllerInteropComposableFqn =
                environment.options["compose-swift-bridge.defaultViewControllerInteropComposableFqn"] ?: DEFAULT_VIEWCONTROLLER_INTEROP_COMPOSABLE,
            defaultUiKitViewInteropComposableFqn =
                environment.options["compose-swift-bridge.defaultUiKitViewInteropComposableFqn"] ?: DEFAULT_UIKITVIEW_INTEROP_COMPOSABLE,
        )

    private fun generatorTargetPlatform(environment: SymbolProcessorEnvironment): GeneratorTarget {
        val targetName = environment.options["compose-swift-bridge.targetName"]

        return if(environment.platforms.size > 1) {
            GeneratorTarget.COMMON
        } else if(environment.platforms.any { it !is NativePlatformInfo }) {
            GeneratorTarget.NON_IOS
        } else {
            val safeTargetName = targetName ?: throw IllegalArgumentException("Missing compose-swift-bridge.targetName, see docs")
            if(safeTargetName.contains("ios", ignoreCase = true)) {
                GeneratorTarget.IOS
            } else {
                GeneratorTarget.NON_IOS
            }
        }
    }
}
