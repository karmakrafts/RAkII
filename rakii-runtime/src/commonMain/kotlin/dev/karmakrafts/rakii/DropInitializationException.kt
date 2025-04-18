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

/**
 * An exception type in the RAkII resource management system used for propagating
 * resource initialization failures in a [DropDelegate] instance.
 * 
 * This exception is thrown when the lazy initialization of a resource managed by
 * RAkII fails. It wraps the original exception that occurred during initialization
 * and provides additional context about the resource that failed to initialize.
 * 
 * In the RAkII resource management lifecycle, this exception plays a critical role
 * in ensuring that even when resource initialization fails, proper cleanup of any
 * partially initialized resources or dependencies still occurs through the drop chain
 * mechanism.
 * 
 * Applications using RAkII should handle this exception to detect and respond to
 * resource initialization failures, potentially implementing fallback strategies
 * or graceful degradation when resources cannot be initialized.
 */
class DropInitializationException(message: String, cause: Throwable? = null) : DropException(message, cause)
