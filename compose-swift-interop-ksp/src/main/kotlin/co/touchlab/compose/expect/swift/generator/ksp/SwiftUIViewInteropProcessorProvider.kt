package co.touchlab.compose.expect.swift.generator.ksp

import co.touchlab.compose.expect.swift.generator.ksp.gen.DEFAULT_FACTORY_NAME
import com.google.devtools.ksp.processing.NativePlatformInfo
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toCamelCase
import java.io.File

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
            defaultFactoryName = environment.options["compose-swift-interop.defaultFactoryName"] ?: DEFAULT_FACTORY_NAME
        )

    private fun generatorTargetPlatform(environment: SymbolProcessorEnvironment): GeneratorTarget {
        val targetName = environment.options["compose-swift-interop.targetName"]

        return if(environment.platforms.size > 1) {
            GeneratorTarget.COMMON
        } else if(environment.platforms.any { it !is NativePlatformInfo }) {
            GeneratorTarget.NON_IOS
        } else {
            val safeTargetName = targetName ?: throw IllegalArgumentException("Missing compose-swift-interop.targetName, see docs")
            if(safeTargetName.contains("ios", ignoreCase = true)) {
                GeneratorTarget.IOS
            } else {
                GeneratorTarget.NON_IOS
            }
        }
    }
}
