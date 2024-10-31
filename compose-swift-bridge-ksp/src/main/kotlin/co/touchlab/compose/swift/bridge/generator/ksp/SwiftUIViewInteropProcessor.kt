package co.touchlab.compose.swift.bridge.generator.ksp

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
import co.touchlab.compose.swift.bridge.generator.ksp.gen.NativeViewInfo
import co.touchlab.compose.swift.bridge.generator.ksp.gen.kotlin.buildLocalCompositionFile
import co.touchlab.compose.swift.bridge.generator.ksp.gen.kotlin.buildNativeViewStateDelegateFiles
import co.touchlab.compose.swift.bridge.generator.ksp.gen.kotlin.buildNativeViewsActualImplementationFiles
import co.touchlab.compose.swift.bridge.generator.ksp.gen.kotlin.buildRawFactoryPerPlatformFiles
import co.touchlab.compose.swift.bridge.generator.ksp.gen.readNativeViewComposable
import co.touchlab.compose.swift.bridge.generator.ksp.util.Types

internal class SwiftUIViewInteropProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val target: GeneratorTarget,
    private val defaultFactoryName: String,
    private val defaultViewControllerInteropComposableFqn: String,
    private val defaultUiKitViewInteropComposableFqn: String,
) : SymbolProcessor {

    private val collectedNativeViews: MutableList<NativeViewInfo> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Types.extensionAnnotation)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.validate() }
            .toList()

        val viewsInfo = symbols.mapNotNull {
            readNativeViewComposable(
                defaultFactoryName = defaultFactoryName,
                defaultViewControllerInteropComposableFqn = defaultViewControllerInteropComposableFqn,
                defaultUiKitViewInteropComposableFqn = defaultUiKitViewInteropComposableFqn,
                logger =  logger,
                function = it,
            )
        }

        collectedNativeViews += viewsInfo

        if(target == GeneratorTarget.IOS) {
            generateNativeViewActuals(
                symbols = symbols,
                viewsInfo = viewsInfo,
                targetName = target.fileSuffix,
            )
        }

        return emptyList()
    }

    override fun finish() {
        when(target) {
            GeneratorTarget.COMMON -> {
                buildLocalCompositionFile(collectedNativeViews)
                    .build()
                    .writeTo(
                        codeGenerator = codeGenerator,
                        aggregating = true,
                    )
            }
            GeneratorTarget.IOS -> {
                generateNativeViewDelegates()
            }
            GeneratorTarget.NON_IOS -> {}
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

    fun generateNativeViewDelegates() {
        val fileSpecs = buildNativeViewStateDelegateFiles(collectedNativeViews)
        for (fileSpec in fileSpecs) {
            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
            )
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