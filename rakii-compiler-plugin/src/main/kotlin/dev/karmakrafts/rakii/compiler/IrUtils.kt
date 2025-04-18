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
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.hasAnnotation

/**
 * Extension function that checks if an IR type is a DropDelegate.
 *
 * This function determines whether the type represents a DropDelegate by comparing
 * its fully qualified name with the DropDelegate class's fully qualified name.
 *
 * @return `true` if the type is a DropDelegate, `false` otherwise
 */
internal fun IrType.isDropDelegate(): Boolean {
    return classFqName == RAkIINames.DropDelegate.fqName
}

/**
 * Extension function that checks if a class should skip drop transformations.
 *
 * This function determines whether a class has been annotated with the
 * `@SkipDropTransforms` annotation, which indicates that the RAkII compiler
 * plugin should not apply any drop-related transformations to this class.
 *
 * @return `true` if the class should skip drop transformations, `false` otherwise
 */
internal fun IrClass.shouldSkipDropTransforms(): Boolean {
    return hasAnnotation(RAkIINames.SkipDropTransforms.id)
}

/**
 * Extension function that checks if a function should skip drop transformations.
 *
 * This function determines whether a function has been annotated with the
 * `@SkipDropTransforms` annotation, which indicates that the RAkII compiler
 * plugin should not apply any drop-related transformations to this function.
 *
 * @return `true` if the function should skip drop transformations, `false` otherwise
 */
internal fun IrSimpleFunction.shouldSkipDropTransforms(): Boolean {
    return hasAnnotation(RAkIINames.SkipDropTransforms.id)
}
