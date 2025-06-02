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

package dev.karmakrafts.rakii.compiler

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal object RAkIINames {
    val packageName: FqName = FqName("dev.karmakrafts.rakii")

    object Functions {
        val defer: Name = Name.identifier("defer")
        val drop: Name = Name.identifier("drop")
        val dropping: Name = Name.identifier("dropping")
        val freeing: Name = Name.identifier("freeing")
        val copyWithoutValue: Name = Name.identifier("copyWithoutValue")
        val copyWithValue: Name = Name.identifier("copyWithValue")
        val dropOnError: Name = Name.identifier("dropOnError")
        val dropOnAnyError: Name = Name.identifier("dropOnAnyError")
        val onError: Name = Name.identifier("onError")
        val onAnyError: Name = Name.identifier("onAnyError")
        val defaultOnError: Name = Name.identifier("defaultOnError")
        val nullOnError: Name = Name.identifier("nullOnError")
    }

    val dropping: CallableId = CallableId(packageName, Functions.dropping)
    val freeing: CallableId = CallableId(packageName, Functions.freeing)

    object DroppingScope {
        val name: Name = Name.identifier("DroppingScope")
        val id: ClassId = ClassId(packageName, name)

        val defer: CallableId = CallableId(packageName, Functions.defer)
        val dropping: CallableId = CallableId(packageName, Functions.dropping)
    }

    object SkipDropTransforms {
        val name: Name = Name.identifier("SkipDropTransforms")
        val id: ClassId = ClassId(packageName, name)
    }

    object Drop {
        val name: Name = Name.identifier("Drop")
        val id: ClassId = ClassId(packageName, name)
    }

    object DropDelegate {
        val name: Name = Name.identifier("DropDelegate")
        val id: ClassId = ClassId(packageName, name)
        val fqName: FqName = id.asSingleFqName()

        val drop: CallableId = CallableId(id, Functions.drop)
        val copyWithoutValue: CallableId = CallableId(id, Functions.copyWithoutValue)
        val copyWithValue: CallableId = CallableId(id, Functions.copyWithValue)
        val dropOnError: CallableId = CallableId(id, Functions.dropOnError)
        val dropOnAnyError: CallableId = CallableId(id, Functions.dropOnAnyError)
        val onError: CallableId = CallableId(id, Functions.onError)
        val onAnyError: CallableId = CallableId(id, Functions.onAnyError)
        val defaultOnError: CallableId = CallableId(id, Functions.defaultOnError)
        val nullOnError: CallableId = CallableId(id, Functions.nullOnError)
    }
}
