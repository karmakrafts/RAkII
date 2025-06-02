package dev.karmakrafts.rakii

import kotlin.jvm.JvmInline
import kotlin.reflect.KProperty

/**
 * A property delegate which exclusively provides a value and offers
 * no additional API surface.
 *
 * ValueDelegate is typically created by terminal operations on [DropDelegate] such as
 * [DropDelegate.nullOnError] or [DropDelegate.defaultOnError], which transform
 * the potentially failing resource operation into a safe, non-failing property access.
 *
 * @param T The nullable type of the value provided by this delegate.
 */
@JvmInline
value class ValueDelegate<T : Any?> @PublishedApi internal constructor(
    @PublishedApi internal val getter: (Any?, KProperty<*>) -> T
) {
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getter(thisRef, property)
}
