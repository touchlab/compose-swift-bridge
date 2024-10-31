package co.touchlab.compose.swift.bridge.generator.skie

import co.touchlab.compose.swift.bridge.GeneratedExpectSwiftView
import co.touchlab.compose.swift.bridge.ViewType
import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationScope
import co.touchlab.skie.configuration.ConfigurationTarget
import co.touchlab.skie.configuration.findAnnotation

object ExpectSwiftVieConfigurationKey : ConfigurationKey.NonOptional<NativeViewConfig>,
    ConfigurationScope.Class {
    override val defaultValue: NativeViewConfig = NativeViewConfig(
        factoryName = "NativeView",
        viewType = ViewType.SwiftUI,
        viewName = "",
    )

    override fun deserialize(value: String?): NativeViewConfig {
        return defaultValue // not supported
    }

    override fun findAnnotationValue(configurationTarget: ConfigurationTarget): NativeViewConfig? {
        return configurationTarget.findAnnotation<GeneratedExpectSwiftView>()?.let {
            NativeViewConfig(
                factoryName = it.factoryName,
                viewType = ViewType.valueOf(it.type),
                viewName = it.viewName,
            )
        }
    }
}