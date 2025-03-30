package dev.karmakrafts.rakii

/**
 * Creates a new [DropDelegate] instance owned by the calling class [OWNER].
 * This leaves the delegate uninitialized until the first time it is referenced.
 *
 * @param TYPE The value type of the delegate.
 * @param OWNER The type of the class which contains the delegate field.
 * @param dropHandler A callback which gets invoked when the given delegate is dropped.
 * @param initializer A factory which is invoked for lazily creating the delegate value when it's used.
 * @return A new [DropDelegate] instance associated with this class.
 */
@IntrinsicDropApi
inline fun <reified TYPE : Any, reified OWNER : Drop> OWNER.dropping(
    noinline dropHandler: (TYPE) -> Unit, noinline initializer: () -> TYPE
): DropDelegate<TYPE, OWNER> {
    return DropDelegate(this, dropHandler, initializer)
}

/**
 * Creates a new [DropDelegate] instance owned by the calling class [OWNER].
 * This leaves the delegate uninitialized until the first time it is referenced.
 * The value must implement the [AutoCloseable] interface or subclass it indirectly,
 * which will cause the [AutoCloseable.close] function to be invoked when the
 * field is dropped.
 *
 * @param TYPE The value type of the delegate which extends [AutoCloseable].
 * @param OWNER The type of the class which contains the delegate field.
 * @param initializer A factory which is invoked for lazily creating the delegate value when it's used.
 * @return A new [DropDelegate] instance associated with this class.
 */
@IntrinsicDropApi
inline fun <reified TYPE : AutoCloseable, reified OWNER : Drop> OWNER.dropping(
    noinline initializer: () -> TYPE
): DropDelegate<TYPE, OWNER> = dropping(AutoCloseable::close, initializer)