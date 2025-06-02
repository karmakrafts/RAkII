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

import co.touchlab.stately.collections.ConcurrentMutableList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A dropping scope instance which provides a runtime implementation
 * for handling end-of-scope dropping using the existing [DropDelegate] API.
 *
 * DroppingScope is a key component of the RAkII resource management system that enables
 * local resource management within function scopes. It allows resources to be automatically
 * cleaned up when execution leaves the scope, regardless of how it exits (normal return or exception).
 *
 * This class provides a structured way to manage resources with deterministic cleanup
 * in local scopes, complementing the class-level resource management provided by the [Drop] interface.
 */
open class DroppingScope @PublishedApi internal constructor() {
    /**
     * A specialized [Drop] implementation that serves as the owner for [DropDelegate] instances
     * created within a [deferring] block.
     *
     * This class is used internally by the RAkII resource management system to provide
     * a consistent owner for delegates created in function scopes, allowing them to be
     * properly managed by the same mechanisms used for class-level resource management.
     *
     * The [SkipDropTransforms] annotation ensures that the RAkII compiler plugin doesn't
     * apply transformations to this class, as it's handled specially by the runtime.
     */
    @SkipDropTransforms
    class Owner private constructor() : Drop {
        /**
         * Companion object providing access to the singleton [Owner] instance.
         */
        companion object {
            /**
             * The singleton instance of [Owner] used for all [DropDelegate] instances
             * created within [deferring] blocks.
             */
            @PublishedApi
            internal val instance: Owner = Owner()
        }

        /**
         * Implementation of the [Drop.drop] method that does nothing.
         *
         * The actual dropping of resources is handled by the [DroppingScope] class
         * through the [dropAll] method, not through this method.
         */
        @GeneratedDropApi
        override fun drop() = Unit
    }

    @PublishedApi
    internal val delegates: ConcurrentMutableList<() -> Unit> = ConcurrentMutableList()

    /**
     * Resets this scope by clearing all registered drop handlers.
     *
     * This method is called by [get] to ensure a clean scope for each new
     * [deferring] block, preventing resource leaks between different blocks.
     *
     * @return This [DroppingScope] instance after resetting.
     */
    @PublishedApi
    internal fun reset(): DroppingScope {
        delegates.clear()
        return this
    }

    /**
     * Executes all registered drop handlers in this scope.
     *
     * This method is called in the finally block of the [deferring] function
     * to ensure all resources are properly cleaned up when the scope ends,
     * regardless of how it exits (normal return or exception).
     */
    @PublishedApi
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun dropAll() = delegates.forEach { it() }

    /**
     * Defers the given code block until the end of the function scope.
     * That is, every possible code-path leading to a return.
     *
     * @param scope The scope to be run before every return-point in the scope.
     */
    fun defer(scope: () -> Unit) {
        delegates += scope
    }

    /**
     * Defers the drop of the given value until the end of the function scope.
     * That is, every possible code-path leading to a return.
     *
     * @param T The value type of the delegate.
     * @param dropHandler A callback which gets invoked when the given delegate is dropped.
     * @param initializer A factory which is invoked for lazily creating the delegate value when it's used.
     * @return A new [DropDelegate] instance associated with the current scope.
     */
    fun <T : Any> dropping( // @formatter:off
        dropHandler: (T) -> Unit,
        initializer: () -> T
    ): DropDelegate<T, Owner> { // @formatter:on
        val delegate = DropDelegate(Owner.instance, dropHandler, initializer)
        delegates += delegate::drop
        return delegate
    }

    /**
     * Defers the drop of the given value until the end of the function scope.
     * That is, every possible code-path leading to a return.
     * The drop of this variable will invoke the [AutoCloseable.close] function
     * of the value if it was initialized until that point in execution.
     *
     * @param T The value type of the delegate.
     * @param initializer A factory which is invoked for lazily creating the delegate value when it's used.
     * @return A new [DropDelegate] instance associated with the current scope.
     */
    fun <T : AutoCloseable> dropping( // @formatter:off
        initializer: () -> T
    ): DropDelegate<T, Owner> { // @formatter:on
        val delegate = DropDelegate(Owner.instance, AutoCloseable::close, initializer)
        delegates += delegate::drop
        return delegate
    }
}

/**
 * Creates a new deferring scope which allows locally auto-dropping
 * values and using guards.
 *
 * This function is a key part of the RAkII resource management system that enables
 * structured resource management within local scopes. It creates a [DroppingScope]
 * and ensures that all resources created within the scope are properly cleaned up
 * when execution leaves the scope, regardless of how it exits (normal return or exception).
 *
 * Use this function when you need to manage resources with deterministic cleanup
 * in a local scope, similar to using try-with-resources in Java or using statements in C#.
 *
 * Example:
 * ```
 * deferring {
 *     val resource = dropping { createResource() }
 *     // Use resource...
 *     // resource will be automatically closed when leaving this scope
 * }
 * ```
 *
 * @param scope The deferring scope function containing resource management code.
 * @return The value returned within the deferring scope if no exception was raised.
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified R> deferring(scope: DroppingScope.() -> R): R {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    val scopeInstance = DroppingScope()
    return try {
        scopeInstance.scope()
    }
    finally {
        scopeInstance.dropAll()
    }
}
