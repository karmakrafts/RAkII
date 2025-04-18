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

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptVoid

/**
 * IR generation extension for the RAkII compiler plugin.
 *
 * This class is responsible for running the IR (Intermediate Representation) lowering passes
 * for the RAkII compiler plugin. It applies the RAkIIDropLoweringVisitor and RAkIILocalDropLoweringVisitor
 * to the module fragment to implement drop functions and process local drop operations.
 */
internal class RAkIILoweringExtension : IrGenerationExtension {
    /**
     * Generates IR code for the RAkII compiler plugin.
     *
     * This method is called by the Kotlin compiler during the IR phase of compilation.
     * It applies the RAkIIDropLoweringVisitor to implement drop functions for classes
     * implementing the Drop interface, and the RAkIILocalDropLoweringVisitor to process
     * local drop operations within function bodies.
     *
     * @param moduleFragment The module fragment to process
     * @param pluginContext The IR plugin context that provides access to IR utilities and services
     */
    override fun generate(
        moduleFragment: IrModuleFragment, pluginContext: IrPluginContext
    ) {
        pluginContext.messageCollector.report(
            CompilerMessageSeverity.LOGGING, "Running RAkII IR lowering pass for ${moduleFragment.name}"
        )
        moduleFragment.acceptVoid(RAkIIDropLoweringVisitor(pluginContext))
        moduleFragment.acceptVoid(RAkIILocalDropLoweringVisitor(pluginContext))
    }
}
