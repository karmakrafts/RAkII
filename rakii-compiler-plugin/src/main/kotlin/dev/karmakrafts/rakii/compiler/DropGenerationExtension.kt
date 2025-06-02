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

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.isClass
import org.jetbrains.kotlin.descriptors.isObject
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.isCompanion
import org.jetbrains.kotlin.fir.declarations.utils.isInterface
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.resolve.getSuperTypes
import org.jetbrains.kotlin.fir.resolve.toClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

internal class DropGenerationExtension(
    session: FirSession, private val messageCollector: MessageCollector
) : FirDeclarationGenerationExtension(session) {
    companion object Key : GeneratedDeclarationKey()

    private fun implementsDropInterface(clazz: FirClassSymbol<*>): Boolean {
        return clazz.getSuperTypes(session).any { type ->
            val clazz = type.toClassSymbol(session) ?: return@any false
            if (!clazz.isInterface) return@any false
            return@any clazz.classId == RAkIINames.Drop.id
        }
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>, context: MemberGenerationContext
    ): Set<Name> {
        // If the class implements the Drop interface, supply a drop function
        if (implementsDropInterface(classSymbol) && !classSymbol.shouldSkipDropTransforms(session)) {
            return setOf(RAkIINames.Functions.drop)
        }
        return emptySet()
    }

    private fun generateDropFunction(clazz: FirClassSymbol<*>): FirSimpleFunction {
        return createMemberFunction(
            owner = clazz,
            key = Key,
            name = RAkIINames.Functions.drop,
            returnType = session.builtinTypes.unitType.coneType
        ) {
            visibility = Visibilities.Public
            status {
                isOverride = true
                isFun = true
            }
        }
    }

    override fun generateFunctions(
        callableId: CallableId, context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        // Make sure our generation context is available
        if (context == null) return emptyList()
        val clazz = context.owner
        val classKind = clazz.classKind
        // Skip any non-regular classes
        if (!classKind.isClass && !classKind.isObject && !clazz.isCompanion) return emptyList()
        // Check if class implements the Drop interface
        if (!implementsDropInterface(clazz) || clazz.shouldSkipDropTransforms(session)) return emptyList()
        messageCollector.report(CompilerMessageSeverity.LOGGING, "Found droppable class ${clazz.classId}")
        // Generate drop function definition
        return listOf(generateDropFunction(clazz).symbol)
    }
}
