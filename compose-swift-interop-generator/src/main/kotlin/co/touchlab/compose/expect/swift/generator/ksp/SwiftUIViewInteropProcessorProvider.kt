package co.touchlab.compose.expect.swift.generator.ksp

import com.google.devtools.ksp.processing.NativePlatformInfo
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toCamelCase
import java.io.File

sealed class GeneratorTarget {
    abstract val suffix: String
    class IOS(
        val targetName: String,
        val swiftOutputPath: File,
    ) : GeneratorTarget() {
        override val suffix: String = ".ios"

        fun getSwiftGenerationSourceDir(): File = swiftOutputPath.apply { mkdirs() }
    }

    class Common : GeneratorTarget() {
        override val suffix: String = ""
    }

    class NonIOS : GeneratorTarget() {
        override val suffix: String = ".nonios"
    }
}

internal class SwiftUIViewInteropProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SwiftUIViewInteropProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            target = generatorTargetPlatform(environment)
        )

    private fun generatorTargetPlatform(environment: SymbolProcessorEnvironment): GeneratorTarget {
        val targetName = environment.options["swiftInterop.targetName"]

        return if(environment.platforms.size > 1) {
            GeneratorTarget.Common()
        } else if(environment.platforms.any { it !is NativePlatformInfo }) {
            GeneratorTarget.NonIOS()
        } else {
            val safeTargetName = targetName ?: throw IllegalArgumentException("Missing swiftInterop.targetName, see docs")
            if(safeTargetName.contains("ios", ignoreCase = true)) {
                GeneratorTarget.IOS(
                    targetName = safeTargetName.toCamelCase(from = CaseFormat.LOWER_UNDERSCORE),
                    swiftOutputPath = File(
                        environment.options["swiftInterop.swiftOutputPath"]
                            ?: throw IllegalArgumentException("Missing swiftInterop.swiftOutputPath, see docs")
                    )
                )
            } else {
                GeneratorTarget.NonIOS()
            }
        }
    }
}
