package dev.karmakrafts.rakii

/**
 * Creates a new [DropDelegate] instance owned by the calling class [OWNER].
 * This leaves the delegate uninitialized until the first time it is referenced.
 *
 * This function is a core part of the RAkII resource management system, providing
 * a convenient way to create resource delegates that will be automatically managed
 * by the RAkII compiler plugin. Resources created with this function will be
 * properly initialized when first accessed and properly cleaned up when the owner
 * is dropped.
 *
 * @param TYPE The value type of the delegate.
 * @param OWNER The type of the class which contains the delegate field.
 * @param dropHandler A callback which gets invoked when the given delegate is dropped.
 * @param initializer A factory which is invoked for lazily creating the delegate value when it's used.
 * @return A new [DropDelegate] instance associated with this class.
 */
inline fun <reified TYPE : Any, reified OWNER : Drop> OWNER.dropping(
    crossinline dropHandler: DropDslScope.(TYPE) -> Unit,
    crossinline initializer: DropDslScope.() -> TYPE
): DropDelegate<TYPE, OWNER> {
    return DropDelegate(this, { DropDslScope.instance.dropHandler(it) }) {
        DropDslScope.instance.initializer()
    }
}

/**
 * Creates a new [DropDelegate] instance owned by the calling class [OWNER].
 * This leaves the delegate uninitialized until the first time it is referenced.
 * 
 * This is a convenience overload for [AutoCloseable] resources in the RAkII system.
 * The value must implement the [AutoCloseable] interface or subclass it indirectly,
 * which will cause the [AutoCloseable.close] function to be invoked when the
 * field is dropped.
 *
 * This function simplifies resource management for standard closeable resources
 * by automatically using the close method as the drop handler, reducing boilerplate
 * code while maintaining the benefits of RAkII's deterministic resource management.
 *
 * @param TYPE The value type of the delegate which extends [AutoCloseable].
 * @param OWNER The type of the class which contains the delegate field.
 * @param initializer A factory which is invoked for lazily creating the delegate value when it's used.
 * @return A new [DropDelegate] instance associated with this class.
 */
inline fun <reified TYPE : AutoCloseable, reified OWNER : Drop> OWNER.dropping(
    crossinline initializer: DropDslScope.() -> TYPE
): DropDelegate<TYPE, OWNER> = dropping({ AutoCloseable::close }) {
    DropDslScope.instance.initializer()
}
