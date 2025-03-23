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

    val dropInterfaceName: Name = Name.identifier("Drop")
    val dropInterfaceId: ClassId = ClassId(packageName, dropInterfaceName)

    val dropDelegateClassName: Name = Name.identifier("DropDelegate")
    val dropDelegateClassId: ClassId = ClassId(packageName, dropDelegateClassName)
    val dropDelegateClassFqName: FqName = dropDelegateClassId.asSingleFqName()

    val dropFunctionName: Name = Name.identifier("drop")

    val delegateDropFunctionId: CallableId = CallableId(dropDelegateClassId, dropFunctionName)
}