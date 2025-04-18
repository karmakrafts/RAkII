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
 * An exception type in the RAkII resource management system which may be raised
 * by a [DropDelegate] instance when one of its error handlers raises an exception.
 * 
 * This exception represents a failure in the error handling mechanism itself,
 * indicating that not only did the primary resource operation fail, but the
 * attempt to handle that failure also failed. This is a more severe condition
 * than a regular [DropException] as it indicates that the error recovery
 * mechanism itself is compromised.
 * 
 * This exception is typically thrown when custom error handlers registered via
 * [DropDelegate.onError] or [DropDelegate.onAnyError] throw exceptions during
 * their execution. Applications should treat this as a serious error condition
 * that may require special recovery procedures.
 */
class DropErrorHandlerException(message: String, cause: Throwable? = null) : DropException(message, cause)
