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

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.visitors.IrVisitor

/*
    class SomeResource : AutoCloseable { ... }

    class Foo : Drop {
        val dependantResource by dropping(::SomeResource)
        val resource by dropping(::SomeResource)
            .dropOnAnyError(Foo::dependantResource)
    }

    \/  - Code generation during lowering

    class Foo : Drop {
        // $dependantResource backing field
        val dependantResource by dropping(::SomeResource)
        // $resource backing field
        val resource by dropping(::SomeResource)
            .dropOnAnyError(Foo::dependantResource)

        override fun drop() {
            $resource.drop()
            $dependantResource.drop()
        }
    }

    \/  - Inlining pass

    class Foo : Drop {
        val dependantResourceIsDropped: AtomicBoolean = AtomicBoolean(false)
        val dependantResource = SomeResource()
        val resource = try {
            SomeResource()
        } catch(error: Throwable) {
            if(!dependantResourceIsDropped.compareAndSet(false, true)) return
            dependantResource.close()
        }

        override fun drop() {
            resource.close()
            if(dependantResourceIsDropped.compareAndSet(false, true)) dependantResource.close()
        }
    }

     */
internal class DropMemberInlineVisitor : IrVisitor<Unit, DropLoweringContext>() {
    override fun visitElement(element: IrElement, data: DropLoweringContext) {
        element.acceptChildren(this, data)
    }
}