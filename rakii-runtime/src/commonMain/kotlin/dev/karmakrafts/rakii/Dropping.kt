package dev.karmakrafts.rakii

@IntrinsicDropApi
fun <TYPE : Any, OWNER : Drop> OWNER.dropping(
    dropHandler: (TYPE) -> Unit,
    initializer: () -> TYPE
): DropDelegate<TYPE, OWNER> {
    return DropDelegate(this, dropHandler, initializer)
}

@IntrinsicDropApi
fun <TYPE : AutoCloseable, OWNER : Drop> OWNER.dropping(
    initializer: () -> TYPE
): DropDelegate<TYPE, OWNER> = dropping(AutoCloseable::close, initializer)