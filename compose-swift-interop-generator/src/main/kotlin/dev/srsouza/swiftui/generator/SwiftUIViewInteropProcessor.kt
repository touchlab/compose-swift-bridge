package dev.srsouza.swiftui.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.srsouza.swiftui.generator.Helpers.Members.composeNativeViewFactory
import dev.srsouza.swiftui.generator.Helpers.Members.nativeViewDelegate
import dev.srsouza.swiftui.generator.Helpers.Members.nativeViewFactory
import dev.srsouza.swiftui.generator.Helpers.Members.nativeViewObservable
import dev.srsouza.swiftui.generator.Helpers.Members.observableObject
import dev.srsouza.swiftui.generator.Helpers.Members.swiftUIViewController
import dev.srsouza.swiftui.generator.Helpers.Members.uiViewController
import dev.srsouza.swiftui.generator.Helpers.factoryFunctionName
import dev.srsouza.swiftui.generator.gen.buildLocalCompositionFile
import dev.srsouza.swiftui.generator.gen.buildNativeViewOrNull
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.ImportSpec
import io.outfoxx.swiftpoet.Modifier
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.toPascalCase
import java.io.File
import io.outfoxx.swiftpoet.FileSpec.Companion as SwiftFileSpec
import io.outfoxx.swiftpoet.ParameterSpec.Companion as SwiftParameterSpec
import io.outfoxx.swiftpoet.PropertySpec.Companion as SwiftPropertySpec
import io.outfoxx.swiftpoet.TypeSpec.Companion as SwiftTypeSpec

data class NativeView(
    val name: String,
    val parameters: List<NativeViewParameter>,
    val factoryName: String,
    val actualFunSpec: FunSpec,
    val file: KSFile,
)

data class NativeViewParameter(
    val name: String,
    val type: TypeName,
    val isModifier: Boolean,
) {
    val namePascalCase = name.toPascalCase(CaseFormat.CAMEL)
}

internal class SwiftUIViewInteropProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val target: GeneratorTarget,
) : SymbolProcessor {

    private val collectedNativeViews: MutableList<NativeView> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Helpers.extensionAnnotation)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.validate() }
            .toList()

        logger.warn("processing swift")

        val nativeViews = symbols.mapNotNull {
            buildNativeViewOrNull(logger, it)
        }

        collectedNativeViews += nativeViews

        if(target is GeneratorTarget.IOS) {
            generateNativeViewActuals(
                symbols = symbols,
                nativeViews = nativeViews,
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
        nativeViews: List<NativeView>,
        targetName: String,
    ) {
        val nativeViewFiles = mutableMapOf<String, FileSpec.Builder>()

        for (nativeView in nativeViews) {
            val fileSpec = nativeViewFiles.getOrPut(nativeView.file.filePath) {
                FileSpec.builder(
                    fileName = "${nativeView.file.fileName.removeSuffix(".kt")}.$targetName",
                    packageName = nativeView.file.packageName.asString(),
                )
            }

            fileSpec.addFunction(nativeView.actualFunSpec)
        }

        nativeViewFiles.values.forEach { it.build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
                originatingKSFiles = symbols.map { it.containingFile!! }
            )
        }
    }

    fun generateNativeViewFactories(target: GeneratorTarget) {
        for ((factoryName, nativeViews) in collectedNativeViews.groupBy { it.factoryName }) {

            val interfaceType = composeNativeViewFactory(factoryName)
            val interfaceSpec = TypeSpec.interfaceBuilder(interfaceType)

            when(target) {
                is GeneratorTarget.Common -> {
                    interfaceSpec.addModifiers(KModifier.EXPECT)
                }
                is GeneratorTarget.IOS -> {
                    interfaceSpec.addModifiers(KModifier.ACTUAL)
                    for (nativeView in nativeViews) {
                        val factoryFunctionName = factoryFunctionName(nativeView.name)
                        val factoryFunctionParameters =
                            nativeView.parameters.filterNot { it.isModifier }

                        val funSpec = FunSpec.builder(factoryFunctionName)
                            .addModifiers(KModifier.ABSTRACT)

                        for (param in factoryFunctionParameters) {
                            funSpec.addParameter(
                                name = param.name,
                                type = param.type
                            )
                        }

                        funSpec.returns(
                            Pair::class.asClassName().parameterizedBy(
                                uiViewController,
                                nativeViewDelegate(nativeView.name)
                            )
                        )

                        interfaceSpec.addFunction(funSpec.build())
                    }
                }

                is GeneratorTarget.NonIOS -> {
                    interfaceSpec.addModifiers(KModifier.ACTUAL)
                }
            }

            FileSpec.builder(
                packageName = interfaceType.packageName,
                fileName = "${interfaceType.simpleName}${target.suffix}"
            )
                .addType(interfaceSpec.build())
                .build()
                .writeTo(
                    codeGenerator = codeGenerator,
                    aggregating = true,
                )
        }
    }

    fun generateSwiftNativeViewFactories(context: GeneratorTarget.IOS) {
        for ((factoryName, nativeViews) in collectedNativeViews.groupBy { it.factoryName }) {

            val protocolName = nativeViewFactory(factoryName)
            val protocolSpec = SwiftTypeSpec.protocolBuilder(protocolName)
                .addModifiers(Modifier.PUBLIC)

            for(nativeView in nativeViews) {
                val factoryFunctionName = factoryFunctionName(nativeView.name)

                val funSpec = FunctionSpec.abstractBuilder(factoryFunctionName)

                funSpec.addParameter(
                    name = "observable",
                    type = nativeViewObservable(nativeView.name)
                )

                funSpec.returns(swiftUIViewController)

                protocolSpec.addFunction(funSpec.build())
            }

            SwiftFileSpec.builder(protocolName)
                .addType(protocolSpec.build())
                .build()
                .writeTo(context.getSwiftGenerationSourceDir())
            logger.warn("Writing $protocolName to ${context.getSwiftGenerationSourceDir().path}")
        }
    }

    fun generateNativeViewDelegates() {
        for(nativeView in collectedNativeViews) {
            val typeName = nativeViewDelegate(nativeView.name)
            val interfaceSpec = TypeSpec.interfaceBuilder(typeName)

            for (param in nativeView.parameters.filterNot { it.isModifier }) {
                interfaceSpec.addFunction(FunSpec.builder(
                    "update${param.namePascalCase}"
                )
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameter(ParameterSpec.builder(param.name, param.type).build())
                    .build())
            }

            FileSpec.builder(typeName).addType(interfaceSpec.build())
                .build()
                .writeTo(
                    codeGenerator = codeGenerator,
                    aggregating = true,
                )
        }
    }

    fun generateSwiftNativeViewObservables(
        context: GeneratorTarget.IOS,
    ) {
        nativeViews@for (nativeView in collectedNativeViews) {
            val type = nativeViewObservable(nativeView.name)
            val classSpec = SwiftTypeSpec.classBuilder(type)

            classSpec.addSuperType(nativeViewDelegate(nativeView.name).toSwift())
            classSpec.addSuperType(observableObject)
            classSpec.addModifiers(Modifier.PUBLIC)

            val initBuilder = FunctionSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
            for (param in nativeView.parameters.filterNot { it.isModifier }) {
                val swiftType = param.type.toSwift()

                if(swiftType == null) {
                    logger.warn("[SwiftGen] Unable to generate Swift Observable for View ${param.name} because of type ${param.type} unable to Map to Swift Type")
                    continue@nativeViews
                }

                classSpec.addProperty(
                    SwiftPropertySpec.varBuilder(param.name, swiftType)
                        .addAttribute("Published")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
                )

                val paramSpec = SwiftParameterSpec.builder(
                    parameterName = param.name,
                    type = swiftType
                ).apply {
                    if(swiftType is FunctionTypeName) {
                        addAttribute("escaping")
                    }
                }

                initBuilder.addParameter(paramSpec.build())

                initBuilder.addCode("self.${param.name} = ${param.name}\n")

                classSpec.addFunction(
                    FunctionSpec.builder("update${param.namePascalCase}")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(paramSpec.build())
                        .addCode("self.${param.name} = ${param.name}\n")
                        .build()
                )
            }

            classSpec.addFunction(initBuilder.build())

            SwiftFileSpec.builder(type.simpleName)
                .addType(classSpec.build())
                .build()
                .writeTo(context.getSwiftGenerationSourceDir())
            logger.warn("Writing ${type.simpleName} to ${context.getSwiftGenerationSourceDir().path}")
        }
    }

    fun generateSwiftNativeViewFactoriesBinding(context: GeneratorTarget.IOS) {
        nativeViews@for ((factoryName, nativeViews) in collectedNativeViews.groupBy { it.factoryName }) {

            val className = "iOS${nativeViewFactory(factoryName)}"
            val classSpec = SwiftTypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperType(composeNativeViewFactory(factoryName).toSwift())

            for(nativeView in nativeViews) {
                val factoryFunctionName = factoryFunctionName(nativeView.name)

                val funSpec = FunctionSpec.builder(factoryFunctionName)
                    .addModifiers(Modifier.PUBLIC)

                for (param in nativeView.parameters.filterNot { it.isModifier }) {
                    val swiftType = param.type.toSwift()

                    if(swiftType == null) {
                        logger.warn("[SwiftGen] Unable to generate Swift Factory binding for View ${param.name} because of type ${param.type} unable to Map to Swift Type")
                        continue@nativeViews
                    }

                    val paramSpec = SwiftParameterSpec.builder(
                        parameterName = param.name,
                        type = swiftType
                    ).apply {
                        if(swiftType is FunctionTypeName) {
                            addAttribute("escaping")
                        }
                    }
                    funSpec.addParameter(paramSpec.build())

                }

                funSpec.returns(
                    Pair::class.asClassName()
                        .parameterizedBy(uiViewController, nativeViewDelegate(nativeView.name))
                        .toSwift()!!
                )

                val createFunctionName = factoryFunctionName(nativeView.name)
                val rawParamsCode = nativeView.parameters.filterNot { it.isModifier }
                    .joinToString { "${it.name}: ${it.name}" }

                funSpec.addCode("""
                    let delegate = %T($rawParamsCode)
                    let viewController = nativeViewFactory.$createFunctionName(
                        observable: delegate
                    )
                    return KotlinPair(first: viewController, second: delegate)
                """.trimIndent(), nativeViewObservable(nativeView.name))

                classSpec.addFunction(funSpec.build())
            }

            val initBuilder = FunctionSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
            val viewFactoryProtocolName = nativeViewFactory(factoryName)
            val viewFactoryParamName = "nativeViewFactory"
            val viewFactoryType = DeclaredTypeName.typeName(".$viewFactoryProtocolName")

            classSpec.addProperty(
                SwiftPropertySpec.builder(viewFactoryParamName, viewFactoryType)
                    .addModifiers(Modifier.PRIVATE)
                    .build()
            )

            initBuilder.addParameter(
                name = viewFactoryParamName,
                type = viewFactoryType,
                label = "_"
            )
            initBuilder.addCode("self.$viewFactoryParamName = $viewFactoryParamName")

            classSpec.addFunction(initBuilder.build())

            SwiftFileSpec.builder(className)
                .addType(classSpec.build())
                .addImport("UIKit")
                .build()
                .writeTo(context.getSwiftGenerationSourceDir())
            logger.warn("Writing $className to ${context.getSwiftGenerationSourceDir().path}")
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