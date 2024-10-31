package co.touchlab.compose.swift.bridge.generator.skie

import co.touchlab.compose.swift.bridge.ViewType
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.sir.type.SirType

data class SwiftNativeViewInfo(
    val delegateRef: KirClass,
    val config: NativeViewConfig,
    val stateParameters: List<NativeViewParameter>,
    val delegateSwiftName: String,
)

data class NativeViewParameter(
    val name: String,
    val type: SirType,
    val updateFunctionName: String,
)

data class NativeViewConfig(
    val factoryName: String,
    val viewType: ViewType,
    val viewName: String,
)
