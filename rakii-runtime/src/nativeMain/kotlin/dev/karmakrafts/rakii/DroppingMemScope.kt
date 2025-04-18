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

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.memScoped
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A delegating implementation of [NativePlacement] for the RAkII resource management system
 * which also provides the drop DSL at the same time.
 *
 * DroppingMemScope combines the native memory allocation capabilities of [MemScope]
 * with the resource management capabilities of [DroppingScope], providing a unified
 * interface for managing both native memory and other resources in Kotlin/Native code.
 *
 * This class is particularly useful when working with native interop code that requires
 * both memory management and resource cleanup, allowing for deterministic cleanup of
 * all resources when execution leaves the scope.
 *
 * @see [DroppingScope] and [MemScope].
 * @param scope The [MemScope] instance delegate of this dropping memory scope.
 */
@ExperimentalForeignApi
@DropDsl
class DroppingMemScope @PublishedApi internal constructor(
    private val scope: MemScope
) : DroppingScope(), NativePlacement by scope

/**
 * Creates a deferring memory scope which provides the
 * [NativePlacement] API and drop DSL at the same time.
 *
 * This function is a key part of the RAkII resource management system for Kotlin/Native,
 * combining the functionality of [deferring] and [memScoped] to provide a unified
 * approach to managing both native memory and other resources.
 *
 * Use this function when working with native interop code that requires both memory
 * management and resource cleanup. It ensures that all resources, including native
 * memory allocations, are properly cleaned up when execution leaves the scope.
 *
 * Example:
 * ```
 * deferringMemScoped {
 *     val nativeStruct = alloc<SomeNativeStruct>() // Memory allocation
 *     val resource = dropping { createResource() }  // Resource allocation
 *     // Use nativeStruct and resource...
 *     // Both will be automatically cleaned up when leaving this scope
 * }
 * ```
 *
 * @see [deferring] and [memScoped].
 * @param scope The deferred memory scope to invoke.
 * @return The value returned from within the given scope.
 */
@ExperimentalForeignApi
@IntrinsicDropApi
@OptIn(ExperimentalContracts::class)
inline fun <reified R> deferringMemScoped(scope: DroppingMemScope.() -> R): R {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    return memScoped {
        val delegate = DroppingMemScope(this)
        try {
            delegate.scope()
        }
        finally {
            delegate.dropAll()
        }
    }
}
