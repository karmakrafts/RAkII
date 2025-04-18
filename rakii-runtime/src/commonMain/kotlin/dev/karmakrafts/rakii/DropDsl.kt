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
 * Marker annotation for drop API DSL container implementations
 * in the RAkII resource management system, such as [DroppingScope].
 * 
 * This annotation serves as a DSL marker in Kotlin's type system to improve
 * the safety and readability of RAkII's resource management DSL. It helps
 * the compiler enforce proper scoping of DSL functions and prevents accidental
 * misuse of nested scopes.
 * 
 * Classes marked with this annotation form the entry points for RAkII's
 * fluent resource management API, providing a structured and type-safe way
 * to define resource lifecycles and cleanup behaviors.
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class DropDsl
