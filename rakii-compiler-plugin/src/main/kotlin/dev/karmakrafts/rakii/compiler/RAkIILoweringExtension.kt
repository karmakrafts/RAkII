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

internal class RAkIILoweringExtension : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment, pluginContext: IrPluginContext
    ) {
        pluginContext.messageCollector.report(
            CompilerMessageSeverity.LOGGING, "Running RAkII IR lowering pass for ${moduleFragment.name}"
        )
        moduleFragment.acceptVoid(DropGenerationVisitor(pluginContext))
        val context = DropLoweringContext()
        moduleFragment.accept(DropDiscoveryVisitor(), context)
        moduleFragment.accept(DropMemberInlineVisitor(), context)
    }
}
