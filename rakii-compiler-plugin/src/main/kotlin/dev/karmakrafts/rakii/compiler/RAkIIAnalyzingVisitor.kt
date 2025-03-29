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
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

internal class RAkIIAnalyzingVisitor(
    private val pluginContext: IrPluginContext
) : IrElementVisitorVoid {
    private val messageCollector: MessageCollector = pluginContext.messageCollector

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitProperty(declaration: IrProperty) {
        super.visitProperty(declaration)

        // Check that we have a backing field and that its type is a DropDelegate
        val backingField = declaration.backingField ?: return
        if (!backingField.type.isDropDelegate()) return
        val backingFieldType = backingField.type as? IrSimpleType ?: return
        val (valueType, _) = backingFieldType.arguments

        // Check that we have a getter on the property which returns
        val getter = declaration.getter ?: return
        val propertyType = getter.returnType
        if (propertyType != valueType) return

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Found delegated droppable property ${declaration.name} in ${declaration.parentClassOrNull?.name}"
        )
    }
}