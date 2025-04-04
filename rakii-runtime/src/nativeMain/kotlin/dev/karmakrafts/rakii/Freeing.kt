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

package dev.karmakrafts.rakii

import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap

/**
 * Creates a new [DropDelegate] instance owned by the calling class [OWNER].
 * This leaves the delegate uninitialized until the first time it is referenced.
 *
 * @param TYPE The C value type of the delegate.
 * @param OWNER The type of the class which contains the delegate field.
 * @param dropHandler A callback which gets invoked when the given delegate is dropped.
 * @param initializer A callback which is invoked for initializing the delegate value when it's used.
 * @return A new [DropDelegate] instance associated with this class.
 */
@ExperimentalForeignApi
@IntrinsicDropApi
inline fun <reified TYPE : CVariable, reified OWNER : Drop> OWNER.freeing( // @formatter:off
    crossinline dropHandler: (TYPE) -> Unit = {},
    crossinline initializer: TYPE.() -> Unit
): DropDelegate<TYPE, OWNER> { // @formatter:on
    return dropping({ value ->
        dropHandler(value)
        nativeHeap.free(value)
    }) {
        val address = nativeHeap.alloc<TYPE>()
        try {
            // Invoke in-line to propagate exceptions
            initializer(address)
        } catch (error: Throwable) {
            nativeHeap.free(address)
            // Ensures we never return a dangling pointer
            throw error
        }
        address
    }
}