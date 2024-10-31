package co.touchlab.compose.expect.swift.generator.ksp.gen.kotlin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import co.touchlab.compose.expect.swift.generator.ksp.GeneratorTarget
import co.touchlab.compose.expect.swift.generator.ksp.gen.NativeViewInfo
import co.touchlab.compose.expect.swift.generator.ksp.gen.ViewType
import co.touchlab.compose.expect.swift.generator.ksp.util.Types
import co.touchlab.compose.expect.swift.generator.ksp.util.Types.Members.composeNativeViewFactory
import co.touchlab.compose.expect.swift.generator.ksp.util.Types.Members.nativeViewFactory
import com.squareup.kotlinpoet.TypeAliasSpec

fun buildRawFactoryPerPlatformFiles(
    allNativeViews: List<NativeViewInfo>,
    target: GeneratorTarget,
): List<FileSpec> {
    val generatedFiles = allNativeViews.groupBy { it.factoryName }
        .map { (factoryName, nativeViews) ->
            buildRawFactoryPerPlatform(
                factoryName = factoryName,
                nativeViews = nativeViews,
                target = target,
            )
        }

    return generatedFiles
}

/**
 * Generate the raw Factory interface that contains
 * all factory functions of the defined Native views
 * annotated with `@ExpectSwiftView` with the giving
 * factoryName.
 *
 * We call this `raw` because this will have the raw values
 * that the Kotlin Native can access, for example UIView
 * and  UIViewController, but we later generate a Swift
 * protocol that is more idiomatic because it can use
 * SwiftUI for example, then, we generate a binding class
 * between the Swift generated Protocol and this interface.
 * See [buildSwiftIdiomaticFactoryFiles] and [buildSwiftViewFactoryProtocolFiles]
 *
 * Because of KSP limitations, not being able to generate
 * common code for iosMain for example, for allowing
 * the generated Factory Interface be available at iosMain
 * we generate a expect interface blank on Common Main,
 * and actual interface for all targets that is not ios
 * with blank body. For ios targets, we generate the actual
 * Factory interface with all factory functions.
 *
 * Because the factory functions is only used by the
 * actual expect view composable generated code for
 * the ios targets, there is no problem on this approach.
 *
 * Example code generated for ios target:
 * ```kotlin
 * public actual interface ComposeNativeViewFactory {
 *   public fun createMapView(coordinate: MapCoordinates, title: String):
 *       Pair<UIViewController, MapViewDelegate>
 * }
 * ```
 */
private fun buildRawFactoryPerPlatform(
    factoryName: String,
    nativeViews: List<NativeViewInfo>,
    target: GeneratorTarget,
): FileSpec {
    val interfaceType = Types.Members.composeNativeViewFactory(factoryName)
    val interfaceSpec = TypeSpec.interfaceBuilder(interfaceType)

    val fileSpec = FileSpec.builder(
        packageName = interfaceType.packageName,
        fileName = "${interfaceType.simpleName}${target.fileSuffix}"
    )

    when(target) {
        GeneratorTarget.COMMON -> {
            // generate expect actual
            interfaceSpec.addModifiers(KModifier.EXPECT)

            // Generates a type alias in the common main with the same name expect
            // iOS protocol to avoid type name confusion between platform.
            fileSpec.addTypeAlias(
                TypeAliasSpec.builder(
                    name = nativeViewFactory(factoryName),
                    type = composeNativeViewFactory(factoryName)
                )
                    .build()
            )
        }
        GeneratorTarget.IOS -> {
            // generate actual for ios target with all factory methods
            interfaceSpec.addModifiers(KModifier.ACTUAL)
            for (viewInfo in nativeViews) {
                val factoryFunctionName = Types.factoryFunctionName(viewInfo.functionName)
                val factoryFunctionParameters =
                    viewInfo.parameters.filterNot { it.isModifier }

                val funSpec = FunSpec.builder(factoryFunctionName)
                    .addModifiers(KModifier.ABSTRACT)

                for (param in factoryFunctionParameters) {
                    funSpec.addParameter(
                        name = param.name,
                        type = param.type
                    )
                }

                val interopTypeBasedOnViewType = when(viewInfo.viewType) {
                    ViewType.SwiftUI,
                    ViewType.UIViewController -> Types.Members.uiViewController
                    ViewType.UIView -> Types.Members.uiView
                }

                funSpec.returns(
                    Pair::class.asClassName().parameterizedBy(
                        interopTypeBasedOnViewType,
                        Types.Members.nativeViewDelegate(viewInfo.functionName)
                    )
                )

                interfaceSpec.addFunction(funSpec.build())
            }
        }

        GeneratorTarget.NON_IOS -> {
            // generate empty actual for non ios platforms
            interfaceSpec.addModifiers(KModifier.ACTUAL)
        }
    }

    return fileSpec
        .addType(interfaceSpec.build())
        .build()
}