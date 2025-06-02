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

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.parentAsClass

internal sealed interface DropDelegateOwner {
    data class Class(val `class`: IrClass) : DropDelegateOwner
    data class DeferringScope(val scope: IrBlockBody) : DropDelegateOwner
}

internal data class DropDelegate(
    val owner: DropDelegateOwner,
    val dropsOnError: Map<IrType, ArrayList<IrProperty>>,
    val doesOnError: Map<IrType, ArrayList<IrBlockBody>>,
    val hasDefaultValue: Boolean,
    val defaultValue: IrExpression?
)

internal fun IrProperty.createDropDelegate(): DropDelegate = DropDelegate(
    owner = DropDelegateOwner.Class(parentAsClass),
    dropsOnError = emptyMap(),
    doesOnError = emptyMap(),
    hasDefaultValue = false,
    defaultValue = null
)

internal fun IrLocalDelegatedProperty.createDropDelegate(): DropDelegate = DropDelegate(
    owner = DropDelegateOwner.DeferringScope(parent as IrBlockBody),
    dropsOnError = emptyMap(),
    doesOnError = emptyMap(),
    hasDefaultValue = false,
    defaultValue = null
)