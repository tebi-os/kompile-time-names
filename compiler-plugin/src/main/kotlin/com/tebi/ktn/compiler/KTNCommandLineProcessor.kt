package com.tebi.ktn.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration


class KTNCommandLineProcessor : CommandLineProcessor {

    override val pluginId = BuildConfig.COMPILER_PLUGIN_ID

    override val pluginOptions = emptyList<CliOption>()

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        error("Unexpected config option: '${option.optionName}'")
    }

}
