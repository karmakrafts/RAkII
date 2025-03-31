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
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
@SkipDropTransforms
class FreeingTest : Drop {
    companion object {
        private const val VALUE: UInt = 0xDEADBEEFU
        private const val ERROR_MESSAGE: String = "Oh oh!"
    }

    @GeneratedDropApi
    override fun drop() = Unit

    @Test
    fun `Lazily initialize`() {
        val delegate = freeing<UIntVar, FreeingTest> { value = VALUE }
        assertNull(delegate.nullableValue)
        assertNotEquals(NativePtr.NULL, delegate.value.rawPtr)
        assertEquals(VALUE, delegate.value.value)
        assertNotNull(delegate.nullableValue)
        delegate.drop()
    }

    @Test
    fun `Update internal drop state`() {
        val delegate = freeing<UIntVar, FreeingTest> { value = VALUE }
        assertFalse(delegate.isDropped)
        assertNull(delegate.nullableValue)
        delegate.drop()
        assertTrue(delegate.isDropped)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Don't invoke drop handler twice on double drop`() {
        var dropCount = 0
        val delegate = freeing<UIntVar, FreeingTest>({ ++dropCount }) { value = VALUE }
        blackHole(delegate.value)
        delegate.drop()
        assertNull(delegate.nullableValue)
        delegate.drop()
        assertNull(delegate.nullableValue)
        assertEquals(1, dropCount)
    }

    @Test
    fun `Rethrow drop error`() {
        val delegate = freeing<UIntVar, FreeingTest>({
            throw IllegalStateException(ERROR_MESSAGE)
        }) { value = VALUE }
        blackHole(delegate.value)
        assertFailsWith<DropException>(ERROR_MESSAGE) {
            delegate.drop()
        }
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Don't invoke drop handler without value`() {
        var isDropped = false
        val delegate = freeing<UIntVar, FreeingTest>({ isDropped = true }) { value = VALUE }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
        delegate.drop()
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Invoke drop handler lazily`() {
        var isDropped = false
        val delegate = freeing<UIntVar, FreeingTest>({ isDropped = true }) { VALUE }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
        blackHole(delegate.value)
        assertNotNull(delegate.nullableValue)
        delegate.drop()
        assertNull(delegate.nullableValue)
        assertTrue(isDropped)
    }

    @Test
    fun `Invoke error callback for any exception type`() {
        var isHandled = false
        val delegate = freeing<UIntVar, FreeingTest>({}) {
            throw IllegalStateException(ERROR_MESSAGE)
            value = VALUE
        }.onAnyError { isHandled = true }
        assertFalse(isHandled)
        assertNull(delegate.nullableValue)
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            println(delegate.value)
        }
        assertTrue(isHandled)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Invoke error callback for matching exception type`() {
        var isHandled = false
        val delegate = freeing<UIntVar, FreeingTest>({}) {
            throw IllegalStateException(ERROR_MESSAGE)
            value = VALUE
        }.onError<IllegalStateException> { isHandled = true }
        assertFalse(isHandled)
        assertNull(delegate.nullableValue)
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            println(delegate.value)
        }
        assertTrue(isHandled)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Don't Invoke error callback for non matching exception type`() {
        var isHandled = false
        val delegate = freeing<UIntVar, FreeingTest>({}) {
            throw IllegalArgumentException(ERROR_MESSAGE)
            value = VALUE
        }.onError<IllegalStateException> { isHandled = true }
        assertFalse(isHandled)
        assertNull(delegate.nullableValue)
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            println(delegate.value)
        }
        assertFalse(isHandled)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Invoke drop chain for any exception type`() {
        var isDropped = false
        val delegate = freeing<UIntVar, FreeingTest>({}) {
            throw IllegalArgumentException(ERROR_MESSAGE)
            VALUE
        }
        delegate.dropChain.getOrPut(Throwable::class) { ArrayList() } += AutoCloseable {
            isDropped = true
        }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            println(delegate.value)
        }
        assertTrue(isDropped)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Invoke drop chain for matching exception type`() {
        var isDropped = false
        val delegate = freeing<UIntVar, FreeingTest>({}) {
            throw IllegalArgumentException(ERROR_MESSAGE)
            VALUE
        }
        delegate.dropChain.getOrPut(IllegalArgumentException::class) { ArrayList() } += AutoCloseable {
            isDropped = true
        }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            println(delegate.value)
        }
        assertTrue(isDropped)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Invoke drop chain for non matching exception type`() {
        var isDropped = false
        val delegate = freeing<UIntVar, FreeingTest>({}) {
            throw IllegalStateException(ERROR_MESSAGE)
            VALUE
        }
        delegate.dropChain.getOrPut(IllegalArgumentException::class) { ArrayList() } += AutoCloseable {
            isDropped = true
        }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            println(delegate.value)
        }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
    }
}