package dev.karmakrafts.rakii

import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
inline fun <reified TYPE : Any, reified OWNER : Drop> OWNER.dropping(
    noinline dropHandler: (TYPE) -> Unit,
    noinline initializer: () -> TYPE
): DropDelegate<TYPE, OWNER> {
    return DropDelegate(this, dropHandler, initializer)
}

inline fun <reified TYPE : AutoCloseable, reified OWNER : Drop> OWNER.dropping(
    noinline initializer: () -> TYPE
): DropDelegate<TYPE, OWNER> = dropping(AutoCloseable::close, initializer)