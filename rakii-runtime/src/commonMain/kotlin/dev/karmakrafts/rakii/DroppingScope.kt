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

import co.touchlab.stately.collections.SharedLinkedList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// TODO: document this
@DropDsl
open class DroppingScope @PublishedApi internal constructor() {
    @SkipDropTransforms
    companion object Owner : Drop {
        @GeneratedDropApi
        override fun drop() = Unit
    }

    @PublishedApi
    internal val delegates: SharedLinkedList<() -> Unit> = SharedLinkedList()

    fun dropAll() = delegates.forEach { it() }

    @IntrinsicDropApi
    fun defer(scope: () -> Unit) {
        delegates += scope
    }

    // TODO: document this
    @IntrinsicDropApi
    inline fun <reified T : Any> dropping( // @formatter:off
        noinline dropHandler: (T) -> Unit,
        noinline initializer: () -> T
    ): DropDelegate<T, Owner> { // @formatter:on
        val delegate = DropDelegate(Owner, dropHandler, initializer)
        delegates += delegate::drop
        return delegate
    }

    // TODO: document this
    @IntrinsicDropApi
    inline fun <reified T : AutoCloseable> dropping( // @formatter:off
        noinline initializer: () -> T
    ): DropDelegate<T, Owner> { // @formatter:on
        val delegate = DropDelegate(Owner, AutoCloseable::close, initializer)
        delegates += delegate::drop
        return delegate
    }
}

// TODO: document this
@OptIn(ExperimentalContracts::class)
inline fun <reified R> deferring(scope: DroppingScope.() -> R): R {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }
    val rtScope = DroppingScope()
    val result = rtScope.scope()
    rtScope.dropAll()
    return result
}