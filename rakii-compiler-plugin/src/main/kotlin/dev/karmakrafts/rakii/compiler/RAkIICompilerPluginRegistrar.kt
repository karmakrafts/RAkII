/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.rakii.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

/**
 * The main entry point for the RAkII compiler plugin.
 *
 * This class is responsible for registering all the necessary extensions for the RAkII compiler plugin
 * to function properly. It registers both FIR (Frontend IR) and IR (Intermediate Representation)
 * extensions that handle the generation and transformation of drop-related code.
 *
 * The plugin is designed to work with the K2 compiler (the new Kotlin compiler implementation).
 */
@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class RAkIICompilerPluginRegistrar : CompilerPluginRegistrar() {
    /**
     * Registers all necessary extensions for the RAkII compiler plugin.
     *
     * This method registers:
     * 1. The FIR extension registrar that handles the generation of drop function declarations
     * 2. The IR generation extension that handles the implementation of drop functions
     *
     * @param configuration The compiler configuration that provides access to compiler settings and services
     */
    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration
    ) {
        FirExtensionRegistrarAdapter.registerExtension(RAkIIFirExtensionRegistrar(configuration.messageCollector))
        IrGenerationExtension.registerExtension(RAkIILoweringExtension())
    }

    /**
     * Indicates that this plugin supports the K2 compiler.
     *
     * The K2 compiler is the new Kotlin compiler implementation that replaces the legacy compiler.
     */
    override val supportsK2: Boolean = true
}
