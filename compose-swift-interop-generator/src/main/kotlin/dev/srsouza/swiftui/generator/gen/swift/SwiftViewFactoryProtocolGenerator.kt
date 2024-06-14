package dev.srsouza.swiftui.generator.gen.swift

import dev.srsouza.swiftui.generator.GeneratorTarget
import dev.srsouza.swiftui.generator.gen.NativeViewInfo
import dev.srsouza.swiftui.generator.util.SwiftFileSpec
import dev.srsouza.swiftui.generator.util.SwiftTypeSpec
import dev.srsouza.swiftui.generator.util.Types
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier

fun buildSwiftViewFactoryProtocolFiles(
    allNativeViews: List<NativeViewInfo>
): List<SwiftFileSpec> {
    return allNativeViews.groupBy { it.factoryName }
        .map { (factoryName, nativeViews) ->
            val protocolName = Types.Members.nativeViewFactory(factoryName)
            val protocolSpec = SwiftTypeSpec.protocolBuilder(protocolName)
                .addModifiers(Modifier.PUBLIC)

            for(viewInfo in nativeViews) {
                val factoryFunctionName = Types.factoryFunctionName(viewInfo.functionName)

                val funSpec = FunctionSpec.abstractBuilder(factoryFunctionName)

                // TODO: allow disable Observable generation
                funSpec.addParameter(
                    name = "observable",
                    type = Types.Members.nativeViewObservable(viewInfo.functionName)
                )

                funSpec.returns(Types.Members.swiftUIViewController)

                protocolSpec.addFunction(funSpec.build())
            }

            SwiftFileSpec.builder(protocolName)
                .addType(protocolSpec.build())
                .build()
        }
}