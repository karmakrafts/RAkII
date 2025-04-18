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
 * Marker annotation for functionality that may be transformed
 * by the RAkII compiler plugin.
 * 
 * In the RAkII resource management system, this annotation identifies
 * functions, properties, and classes that are subject to compile-time
 * transformations by the RAkII compiler plugin. These transformations
 * enable the deterministic resource management capabilities of RAkII.
 * 
 * Functions and properties marked with this annotation may behave
 * differently at runtime than their source code suggests, as the
 * compiler plugin may insert additional code for resource tracking,
 * initialization, and cleanup.
 * 
 * This annotation serves as a visual indicator for:
 * 1. Library users - to understand which APIs are part of RAkII's core functionality
 * 2. Library developers - to identify code that has special handling in the compiler
 * 3. Compiler plugin developers - to locate code that requires transformation
 */
@Retention(AnnotationRetention.BINARY)
annotation class IntrinsicDropApi
