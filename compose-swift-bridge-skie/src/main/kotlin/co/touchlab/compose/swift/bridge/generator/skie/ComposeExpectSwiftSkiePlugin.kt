package co.touchlab.compose.swift.bridge.generator.skie

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.swift.ConvertSirIrFilesToSourceFilesPhase
import co.touchlab.skie.spi.SkiePluginRegistrar

class ComposeExpectSwiftSkiePlugin : SkiePluginRegistrar {

    override val customConfigurationKeys: Set<ConfigurationKey<*>> = setOf(
        ExpectSwiftVieConfigurationKey,
    )

    override fun register(initPhaseContext: InitPhase.Context) {
        initPhaseContext.skiePhaseScheduler.sirPhases.modify {
            val indexOfCodeGenerator = indexOfFirst { phase -> phase is ConvertSirIrFilesToSourceFilesPhase }
            add(indexOfCodeGenerator, ComposeExpectSwiftGenerationPhase)
        }
    }
}