package co.touchlab.compose.expect.swift.generator.skie

import co.touchlab.compose.expect.swift.ViewType
import co.touchlab.compose.expect.swift.generator.skie.generation.swift.buildNativeViewStateSwiftUIObservableObject
import co.touchlab.compose.expect.swift.generator.skie.generation.swift.buildSwiftIdiomaticFactory
import co.touchlab.compose.expect.swift.generator.skie.generation.swift.buildSwiftViewFactoryProtocol
import co.touchlab.compose.expect.swift.generator.skie.generation.swift.extensionPackage
import co.touchlab.compose.expect.swift.generator.skie.generation.swift.nativeViewFactory
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.swift.toValidSwiftIdentifier


object ComposeExpectSwiftGenerationPhase : SirPhase {
    context(SirPhase.Context)
    override suspend fun execute() {
        val annotations = kirProvider.kotlinClasses
            .filter { it.configuration.has(ExpectSwiftVieConfigurationKey) }
            .map { it to it.configuration.get(ExpectSwiftVieConfigurationKey) }

        val viewsInfo: List<SwiftNativeViewInfo> = annotations.map { (ref, config) ->
            val delegateSwiftName = ref.swiftName
            val stateParameters = ref.callableDeclarations.filterIsInstance<KirSimpleFunction>()
                .filter { it.kotlinName.startsWith("update") }
                .map {
                    val parameter = it.valueParameters.first()
                    NativeViewParameter(
                        name = parameter.kotlinName,
                        type = parameter.originalSirValueParameter!!.type,
                        updateFunctionName = it.originalSirFunction.identifier,
                    )
                }

            SwiftNativeViewInfo(
                delegateRef = ref,
                config = config,
                stateParameters = stateParameters,
                delegateSwiftName = delegateSwiftName,
            )
        }

        viewsInfo.filter { it.config.viewType == ViewType.SwiftUI }
            .forEach { viewInfo ->
                sirFileProvider.getWrittenSourceFile(
                    viewInfo.delegateRef.module.name.toValidSwiftIdentifier(),
                    "${viewInfo.config.viewName}Observable"
                ).apply {
                    content = buildString {
                        buildNativeViewStateSwiftUIObservableObject(
                            viewInfo
                        ).writeTo(this)
                    }
                }
            }

        val refModule = viewsInfo.firstOrNull()?.delegateRef?.module
        if(refModule != null) {
            viewsInfo.groupBy { it.config.factoryName }
                .map { (factoryName, views) ->
                    // Generate the native view factory protocol
                    sirFileProvider.getWrittenSourceFile(
                        refModule.name.toValidSwiftIdentifier(),
                        nativeViewFactory(factoryName)
                    ).apply {
                        content = buildString {
                            buildSwiftViewFactoryProtocol(
                                factoryName = factoryName,
                                nativeViews = views
                            ).writeTo(this)
                        }
                    }

                    val composeNativeViewFactory = kirProvider.getClassByFqName(
                        "${extensionPackage}.Compose${nativeViewFactory(factoryName)}"
                    )
                    val kotlinPair = kirProvider.getClassByFqName(
                        "kotlin.Pair"
                    )

                    // Generate the binding between protocol and kotlin protocol
                    sirFileProvider.getWrittenSourceFile(
                        refModule.name.toValidSwiftIdentifier(),
                        "iOS${factoryName}Factory"
                    ).apply {
                        content = buildString {
                            buildSwiftIdiomaticFactory(
                                factoryName = factoryName,
                                nativeViews = views,
                                composeNativeViewFactory = composeNativeViewFactory,
                                kotlinPair = kotlinPair.originalSirClass.toType()
                            ).writeTo(this)
                        }
                    }
                }

        }



        //sirFileProvider.getIrFile(namespace = "")
        //sirFileProvider

    }
}


