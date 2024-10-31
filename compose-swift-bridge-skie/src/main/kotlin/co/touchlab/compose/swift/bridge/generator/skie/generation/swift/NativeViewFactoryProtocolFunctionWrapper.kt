package co.touchlab.compose.swift.bridge.generator.skie.generation.swift

import co.touchlab.compose.swift.bridge.generator.skie.SwiftNativeViewInfo
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.type.DeclaredKirType
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirValueParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.receiverDeclaration
import co.touchlab.skie.sir.type.OirDeclaredSirType
import co.touchlab.skie.util.swift.addFunctionDeclarationBodyWithErrorTypeHandling
import co.touchlab.skie.util.swift.escapeSwiftIdentifier
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.TypeName

context(SirPhase.Context)
internal fun generateReplacementForFunctionsWithViewFactoryParameter(
    viewsInfo: List<SwiftNativeViewInfo>
) {
    val allExportedFactoryNameFqn = viewsInfo.map { it.config.factoryName }
        .distinct()
        .map { it to composeNativeViewFactoryFqn(it) }

    val exportedFunctionsWithNativeFactory: Map<KirSimpleFunction, List<Pair<String, SirClass>>> =
        kirProvider.kotlinFunctions.filterIsInstance<KirSimpleFunction>()
            .mapNotNull { func ->
                val factoryNames = func.valueParameters
                    .mapNotNull { (it.type as? DeclaredKirType?)?.declaration }
                    .mapNotNull {
                        val fqn = it.kotlinFqName
                        val nativeFactoryName = allExportedFactoryNameFqn.find { fqn == it.second }?.first
                        if (nativeFactoryName != null) {
                            nativeFactoryName to it.originalSirClass
                        } else {
                            null
                        }
                    }

                if(factoryNames.isEmpty()) {
                    null
                } else {
                    func to factoryNames
                }
            }
            .toMap()

    for ((function, nativeViewFactoryNames) in exportedFunctionsWithNativeFactory) {
        // todo: find a better way to replace the function
        function.bridgedSirFunction?.isReplaced = true
        function.bridgedSirFunction?.visibility = SirVisibility.Private

        function.primarySirFunction.parent.apply {
            function.bridgedSirFunction = SirSimpleFunction(
                identifier = function.originalSirFunction.identifier,
                returnType = function.originalSirFunction.returnType
            ).apply {
                val parametersCallCodeBlock = function.primarySirFunction.valueParameters.map { parameter ->
                    val objcSirClassOrNull = (parameter.type as? OirDeclaredSirType)?.declaration?.originalSirClass
                    val nativeFactoryNameOrNull = nativeViewFactoryNames.find { it.second.fqName == objcSirClassOrNull?.fqName }?.first

                    if (nativeFactoryNameOrNull != null) {
                        // replace parameter with view factory protocol
                        val protocolSirClass = SirClass(
                            baseName = nativeViewFactory(nativeFactoryNameOrNull),
                            kind = SirClass.Kind.Protocol,
                            parent = sirProvider.getExternalModule(objcSirClassOrNull!!.module.name).builtInFile,
                            superTypes = emptyList(),
                        )
                        val iosViewFactorySirClass = SirClass(
                            baseName = iOSNativeViewFactory(nativeFactoryNameOrNull),
                            kind = SirClass.Kind.Protocol,
                            parent = sirProvider.getExternalModule(objcSirClassOrNull!!.module.name).builtInFile,
                            superTypes = emptyList(),
                        )

                        val parameter = SirValueParameter(
                            label = parameter.label,
                            name = parameter.name,
                            type = protocolSirClass.toType(),
                            inout = parameter.inout,
                        )

                        CodeBlock.toString(
                            "%T(${parameter.name.escapeSwiftIdentifier()})",
                            iosViewFactorySirClass.defaultType.evaluate().swiftPoetTypeName
                        )
                    } else {
                        // just copy the parameter
                        val parameter = SirValueParameter(
                            label = parameter.label,
                            name = parameter.name,
                            type = parameter.type,
                            inout = parameter.inout,
                        )

                        parameter.name.escapeSwiftIdentifier()
                    }
                }
                val sirFunction = function.originalSirFunction

                addFunctionDeclarationBodyWithErrorTypeHandling(sirFunction) {
                    addStatement(
                        "return %L%L%T.%L",
                        if (sirFunction.throws) "try " else "",
                        if (sirFunction.isAsync) "await " else "",
                        sirFunction.kotlinStaticMemberOwnerTypeName,
                        sirFunction.call(parametersCallCodeBlock),
                    )
                }
            }
        }
    }
}

private val SirCallableDeclaration.kotlinStaticMemberOwnerTypeName: TypeName
    get() {
        val owner = receiverDeclaration ?: error("Callable declarations from Kotlin should always have an owner. Was: $this")

        return owner.defaultType.evaluate().swiftPoetTypeName
    }