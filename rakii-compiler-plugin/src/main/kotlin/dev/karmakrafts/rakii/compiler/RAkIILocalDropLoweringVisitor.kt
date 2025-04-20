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

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * IR visitor for processing local drop operations within function bodies.
 *
 * This visitor is part of the RAkII compiler plugin's IR (Intermediate Representation) phase.
 * It is designed to process functions and potentially transform their bodies to handle local
 * drop operations.
 *
 * @property pluginContext The IR plugin context that provides access to IR utilities and services
 */
internal class RAkIILocalDropLoweringVisitor(
    private val pluginContext: IrPluginContext
) : IrElementVisitorVoid {
    /**
     * Message collector used to report compiler messages during the lowering process.
     */
    private val messageCollector: MessageCollector = pluginContext.messageCollector

    /**
     * Visits an IR element and processes its children.
     *
     * @param element The IR element to visit
     */
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    /**
     * Visits a simple function and potentially transforms its body to handle local drop operations.
     *
     * This method checks if a function is marked with @SkipDropTransforms and skips it if so.
     *
     * @param declaration The function to visit
     */
    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        super.visitSimpleFunction(declaration)
        // If the function is marked with @SkipDropTransforms, skip it..
        if (declaration.shouldSkipDropTransforms()) return
        val body = declaration.body as? IrBlockBody ?: return
        for(statement in body.statements) {}
    }
}
