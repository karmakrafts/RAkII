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
 * Creates a new [DropDelegate] instance for a native memory allocation of type [TYPE],
 * owned by the calling class [OWNER].
 *
 * This function is a specialized version of [dropping] for Kotlin/Native that handles
 * native memory allocations (CVariables). It ensures proper memory management by:
 * 1. Allocating memory on the native heap when the value is first accessed
 * 2. Initializing the memory with the provided initializer function
 * 3. Freeing the memory when the owner is dropped
 * 4. Properly handling exceptions during initialization to prevent memory leaks
 *
 * The memory is allocated lazily when the delegate is first accessed, not when it's created.
 *
 * Example usage:
 * ```kotlin
 * class NativeResourceManager : Drop {
 *     val intVar by freeing<IntVar> {
 *         it.value = 42 // Initialize the native memory
 *     }
 * }
 * ```
 *
 * @param TYPE The CVariable type to allocate on the native heap.
 * @param OWNER The type of the class which contains the delegate field.
 * @param dropHandler Optional callback which gets invoked before the memory is freed.
 * @param initializer Function to initialize the allocated memory.
 * @return A [DropDelegate] that manages the lifecycle of the native memory allocation.
 */
@ExperimentalForeignApi
inline fun <reified TYPE : CVariable, reified OWNER : Drop> OWNER.freeing( // @formatter:off
    crossinline dropHandler: (TYPE) -> Unit = {},
    crossinline initializer: (TYPE) -> Unit
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

/**
 * Creates a new [DropDelegate] instance for a native memory allocation of type [TYPE],
 * owned by the current [DroppingScope].
 *
 * This function is a specialized version of [DroppingScope.dropping] for Kotlin/Native
 * that handles native memory allocations (CVariables) within a scope. It ensures proper
 * memory management by:
 * 1. Allocating memory on the native heap when the value is first accessed
 * 2. Initializing the memory with the provided initializer function
 * 3. Freeing the memory when the scope ends
 * 4. Properly handling exceptions during initialization to prevent memory leaks
 *
 * The memory is allocated lazily when the delegate is first accessed, not when it's created.
 *
 * Example usage:
 * ```kotlin
 * deferring {
 *     val intVar by freeing<IntVar> {
 *         it.value = 42 // Initialize the native memory
 *     }
 *     // Use intVar.value here
 *     // Memory is automatically freed when the deferring block exits
 * }
 * ```
 *
 * @param TYPE The CVariable type to allocate on the native heap.
 * @param dropHandler Optional callback which gets invoked before the memory is freed.
 * @param initializer Function to initialize the allocated memory.
 * @return A [DropDelegate] that manages the lifecycle of the native memory allocation.
 */
@ExperimentalForeignApi
inline fun <reified TYPE : CVariable> DroppingScope.freeing( // @formatter:off
    crossinline dropHandler: (TYPE) -> Unit = {},
    crossinline initializer: (TYPE) -> Unit
): DropDelegate<TYPE, DroppingScope.Owner> { // @formatter:on
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
