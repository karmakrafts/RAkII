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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DropDelegateTest {
    companion object {
        private const val VALUE: String = "Hello, World!"
        private const val ERROR_MESSAGE: String = "Oh oh!"
    }

    private object DummyDrop : Drop {
        @GeneratedDropApi
        override fun drop() {
        }
    }

    @Test
    fun `Lazily initialize`() {
        val delegate = DropDelegate(DummyDrop, {}) { VALUE }
        assertNull(delegate.nullableValue)
        assertEquals(VALUE, delegate.value)
        assertNotNull(delegate.nullableValue)
    }

    @Test
    fun `Update internal drop state`() {
        val delegate = DropDelegate(DummyDrop, {}) { VALUE }
        assertFalse(delegate.isDropped)
        assertNull(delegate.nullableValue)
        delegate.drop()
        assertTrue(delegate.isDropped)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Don't invoke drop handler twice on double drop`() {
        var dropCount = 0
        val delegate = DropDelegate(DummyDrop, { ++dropCount }) { VALUE }
        blackHole(delegate.value)
        delegate.drop()
        assertNull(delegate.nullableValue)
        delegate.drop()
        assertNull(delegate.nullableValue)
        assertEquals(1, dropCount)
    }

    @Test
    fun `Rethrow initialization error`() {
        val delegate = DropDelegate(DummyDrop, {}) {
            throw IllegalStateException(ERROR_MESSAGE)
            VALUE
        }
        assertFailsWith<DropInitializationException>(ERROR_MESSAGE) {
            blackHole(delegate.value)
        }
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Rethrow drop error`() {
        val delegate = DropDelegate(DummyDrop, {
            throw IllegalStateException(ERROR_MESSAGE)
        }) { VALUE }
        blackHole(delegate.value)
        assertFailsWith<DropException>(ERROR_MESSAGE) {
            delegate.drop()
        }
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Don't invoke drop handler without value`() {
        var isDropped = false
        val delegate = DropDelegate(DummyDrop, { isDropped = true }) { VALUE }
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
        delegate.drop()
        assertFalse(isDropped)
        assertNull(delegate.nullableValue)
    }

    @Test
    fun `Invoke drop handler lazily`() {
        var isDropped = false
        val delegate = DropDelegate(DummyDrop, { isDropped = true }) { VALUE }
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
        val delegate = DropDelegate(DummyDrop, {}) {
            throw IllegalStateException(ERROR_MESSAGE)
            VALUE
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
        val delegate = DropDelegate(DummyDrop, {}) {
            throw IllegalStateException(ERROR_MESSAGE)
            VALUE
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
        val delegate = DropDelegate(DummyDrop, {}) {
            throw IllegalArgumentException(ERROR_MESSAGE)
            VALUE
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
        val delegate = DropDelegate(DummyDrop, {}) {
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
        val delegate = DropDelegate(DummyDrop, {}) {
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
        val delegate = DropDelegate(DummyDrop, {}) {
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