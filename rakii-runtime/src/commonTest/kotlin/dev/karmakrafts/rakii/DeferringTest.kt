package dev.karmakrafts.rakii

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

class DeferringTest {
    companion object {
        private const val VALUE: String = "HELLORLD"
    }

    @Test
    fun `Invoke defer blocks`() {
        var isInvoked = false
        deferring {
            defer { isInvoked = true }
            assertFalse(isInvoked)
        }
        assertTrue(isInvoked)
    }

    @Test
    fun `Propagate exceptions from defer blocks`() {
        var wasReached = false
        assertFailsWith<RuntimeException>(VALUE) {
            deferring {
                defer { throw RuntimeException(VALUE) }
                wasReached = true
            }
        }
        assertTrue(wasReached)
    }

    @Test
    fun `Propagate exceptions from deferring scope`() {
        var isInvoked = false
        assertFailsWith<RuntimeException>(VALUE) {
            deferring {
                defer { isInvoked = true }
                throw RuntimeException(VALUE)
            }
        }
        assertTrue(isInvoked)
    }

    @Test
    fun `Drop initialized delegate`() {
        var isDropped = false
        deferring {
            val value by dropping({ isDropped = true }) { VALUE }
            assertFalse(isDropped)
            assertEquals(VALUE, value)
        }
        assertTrue(isDropped)
    }

    @Test
    fun `Drop uninitialized delegate`() {
        var isDropped = false
        deferring {
            assertFalse(isDropped)
        }
        assertFalse(isDropped)
    }
}