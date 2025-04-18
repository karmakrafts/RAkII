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
 * A base exception type for all [DropDelegate] related functions in the RAkII resource management system.
 * 
 * This exception represents errors that occur during resource management operations,
 * particularly during the cleanup phase when resources are being dropped. It provides
 * a consistent error handling mechanism for the RAkII system, allowing applications
 * to catch and handle resource management errors in a structured way.
 * 
 * This exception can be raised in several scenarios:
 * 1. Directly when invoking the drop-chain of a [DropDelegate] instance
 * 2. When a drop handler fails to complete successfully
 * 3. Indirectly when a [DropErrorHandlerException] or [DropInitializationException] is raised
 * 
 * Applications using RAkII should handle this exception type to properly respond to
 * resource management failures and potentially implement recovery strategies.
 */
open class DropException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
