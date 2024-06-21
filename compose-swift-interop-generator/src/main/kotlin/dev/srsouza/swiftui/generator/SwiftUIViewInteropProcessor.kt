package dev.srsouza.swiftui.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.writeTo
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.gen.ViewType
import dev.srsouza.swiftui.generator.gen.kotlin.buildLocalCompositionFile
import dev.srsouza.swiftui.generator.gen.kotlin.buildNativeViewStateDelegateFiles
import dev.srsouza.swiftui.generator.gen.kotlin.buildNativeViewsActualImplementationFiles
import dev.srsouza.swiftui.generator.gen.kotlin.buildRawFactoryPerPlatformFiles
import dev.srsouza.swiftui.generator.gen.readNativeViewComposable
import dev.srsouza.swiftui.generator.gen.swift.buildNativeViewStateSwiftUIObservableObjectFiles
import dev.srsouza.swiftui.generator.gen.swift.buildSwiftIdiomaticFactoryFiles
import dev.srsouza.swiftui.generator.gen.swift.buildSwiftViewFactoryProtocolFiles
import dev.srsouza.swiftui.generator.util.Types

internal class SwiftUIViewInteropProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val target: GeneratorTarget,
) : SymbolProcessor {

    private val collectedNativeViews: MutableList<NativeViewInfo> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Types.extensionAnnotation)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.validate() }
            .toList()

        logger.warn("processing swift")

        val viewsInfo = symbols.mapNotNull {
            readNativeViewComposable(logger, it)
        }

        collectedNativeViews += viewsInfo

        if(target is GeneratorTarget.IOS) {
            generateNativeViewActuals(
                symbols = symbols,
                viewsInfo = viewsInfo,
                targetName = target.targetName,
            )
        }

        return emptyList()
    }

    override fun finish() {
        when(target) {
            is GeneratorTarget.Common -> {
                buildLocalCompositionFile(collectedNativeViews)
                    .build()
                    .writeTo(
                        codeGenerator = codeGenerator,
                        aggregating = true,
                    )
            }
            is GeneratorTarget.IOS -> {
                generateNativeViewDelegates()

                generateSwiftNativeViewFactories(target)

                generateSwiftNativeViewObservables(target)

                generateSwiftNativeViewFactoriesBinding(target)
            }
            is GeneratorTarget.NonIOS -> {}
        }

        generateNativeViewFactories(target)

        super.finish()
    }

    private fun generateNativeViewActuals(
        symbols: List<KSFunctionDeclaration>,
        viewsInfo: List<NativeViewInfo>,
        targetName: String,
    ) {
        val files = buildNativeViewsActualImplementationFiles(
            nativeViews = viewsInfo,
            targetName = targetName,
        )

        for (file in files) {
            file.writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
                originatingKSFiles = symbols.map { it.containingFile!! }
            )
        }
    }

    fun generateNativeViewFactories(target: GeneratorTarget) {
        val files = buildRawFactoryPerPlatformFiles(collectedNativeViews, target)

        for(fileSpec in files) {
            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
            )
        }
    }

    fun generateSwiftNativeViewFactories(context: GeneratorTarget.IOS) {
        val fileSpecs = buildSwiftViewFactoryProtocolFiles(collectedNativeViews)

        for (fileSpec in fileSpecs) {
            fileSpec.writeTo(context.getSwiftGenerationSourceDir())
            logger.warn("Writing ${fileSpec.name} to ${context.getSwiftGenerationSourceDir().path}")
        }
    }

    fun generateNativeViewDelegates() {
        val fileSpecs = buildNativeViewStateDelegateFiles(collectedNativeViews)
        for (fileSpec in fileSpecs) {
            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
            )
        }
    }

    fun generateSwiftNativeViewObservables(
        context: GeneratorTarget.IOS,
    ) {
        val swiftUiCollectedNativeViews = collectedNativeViews.filter {
            // Generate only for SwiftUI type
            when (it.viewType) {
                ViewType.SwiftUI -> true

                ViewType.UIViewController,
                ViewType.UIView -> false
            }
        }

        val fileSpecs = buildNativeViewStateSwiftUIObservableObjectFiles(swiftUiCollectedNativeViews)
        for (fileSpec in fileSpecs) {
            fileSpec.writeTo(context.getSwiftGenerationSourceDir())
            logger.warn("Writing ${fileSpec.name} to ${context.getSwiftGenerationSourceDir().path}")
        }
    }

    fun generateSwiftNativeViewFactoriesBinding(context: GeneratorTarget.IOS) {
        val files = buildSwiftIdiomaticFactoryFiles(collectedNativeViews)
        for(fileSpec in files) {
            fileSpec.writeTo(context.getSwiftGenerationSourceDir())
            logger.warn("Writing ${fileSpec.name} to ${context.getSwiftGenerationSourceDir().path}")
        }
    }
}

fun KSValueParameter.getKModifiers(): List<KModifier> {
    return listOfNotNull(
        KModifier.CROSSINLINE.takeIf { isCrossInline },
        KModifier.NOINLINE.takeIf { isNoInline },
        KModifier.VARARG.takeIf { isVararg },
    )
}