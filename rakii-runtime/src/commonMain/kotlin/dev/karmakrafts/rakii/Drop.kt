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

@file:JvmName("Drop$")

package dev.karmakrafts.rakii

import kotlin.jvm.JvmName

/**
 * Marker interface for droppable objects.
 * Droppable objects may be used like regular [AutoCloseable] objects,
 * except that they also provide a generated [drop] function, which
 * should never be invoked directly and which is used internally.
 *
 * Also, [drop] should **NEVER** be overridden manually, hence the required opt-in.
 * Overriding the function manually will cause an error in the RAkII compiler.
 */
interface Drop : AutoCloseable {
    @GeneratedDropApi
    fun drop() {
        throw UnsupportedOperationException("RAkII drop function wasn't lowered during IR transformation")
    }

    @OptIn(GeneratedDropApi::class)
    override fun close() = drop()
}