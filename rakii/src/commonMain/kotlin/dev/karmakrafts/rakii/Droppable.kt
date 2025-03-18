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

@file:JvmName("Droppable$")

package dev.karmakrafts.rakii

import kotlin.jvm.JvmName

@PublishedApi
internal expect fun registerDroppable(instance: Droppable)

fun interface Droppable {
    fun drop()
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Droppable> T.dropDead(): T {
    registerDroppable(this)
    return this
}