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
 * A delegating implementation of [NativePlacement]
 * which also provides the drop DSL at the same time.
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
        val result = delegate.scope()
        delegate.dropAll()
        result
    }
}