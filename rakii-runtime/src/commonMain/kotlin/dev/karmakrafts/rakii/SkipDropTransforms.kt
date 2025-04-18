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
 * A marker annotation for the RAkII resource management system that indicates
 * the annotated class or function should be excluded from automatic transformations
 * by the RAkII compiler plugin.
 * 
 * In the normal operation of RAkII, the compiler plugin automatically transforms
 * classes implementing the [Drop] interface to insert resource management code.
 * This annotation provides an escape hatch from this automatic behavior when:
 * 
 * 1. Custom resource management logic is required that differs from RAkII's standard approach
 * 2. The class has special requirements that aren't compatible with automatic transformations
 * 3. The developer needs complete control over the resource lifecycle
 * 4. The class is part of the RAkII runtime itself and needs to avoid circular dependencies
 * 
 * When using this annotation, the developer takes full responsibility for implementing
 * proper resource management, including correctly implementing the [Drop.drop] method
 * and ensuring all resources are properly cleaned up.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class SkipDropTransforms
