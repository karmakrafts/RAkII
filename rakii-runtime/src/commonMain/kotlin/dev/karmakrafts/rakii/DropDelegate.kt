package dev.karmakrafts.rakii

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

@OptIn(ExperimentalAtomicApi::class)
class DropDelegate<TYPE : Any, OWNER : Drop> @PublishedApi internal constructor(
    @PublishedApi internal val instance: OWNER,
    @PublishedApi internal val dropHandler: (TYPE) -> Unit,
    @PublishedApi internal val initializer: () -> TYPE
) {
    @PublishedApi
    internal val dropChain: HashMap<KClass<out Throwable>, ArrayList<AutoCloseable>> by lazy { HashMap() }

    @PublishedApi
    internal val errorHandlers: HashMap<KClass<out Throwable>, ArrayList<(Throwable) -> Unit>> by lazy { HashMap() }

    @PublishedApi
    internal var _isDropped: AtomicBoolean = AtomicBoolean(false)

    @PublishedApi
    internal var _value: AtomicReference<TYPE?> = AtomicReference(null)

    /**
     * Indicates whether this value has been dropped.
     */
    inline val isDropped: Boolean
        get() = _isDropped.load()

    /**
     * The nullable value of this delegate.
     * This will return null if the delegate hasn't been lazily
     * initialized before.
     */
    inline val nullableValue: TYPE?
        get() = _value.load()

    /**
     * The value of this delegate.
     * This will cause the delegate to be lazily initialized
     * if it wasn't referenced before.
     */
    val value: TYPE
        get() {
            val currentValue = _value.load()
            if (currentValue == null) {
                try {
                    val newValue = initializer()
                    _value.store(newValue)
                    return newValue
                } catch (error: Throwable) {
                    // Invoke all handlers which are applicable to the error
                    invokeDropChain(error)
                    invokeErrorHandlers(error)
                    // Propagate error to the caller of the delegate
                    throw DropInitializationException("Failed to initialize dropping property", error)
                }
            }
            return currentValue
        }

    private fun invokeDropChain(error: Throwable) {
        for ((type, chain) in dropChain) {
            if (!type.isInstance(error)) continue
            for (link in chain) {
                try {
                    link.close()
                } catch (error: Throwable) {
                    throw DropException("Drop chain did not complete", error)
                }
            }
        }
    }

    private fun invokeErrorHandlers(error: Throwable) {
        for ((type, handlers) in errorHandlers) {
            if (!type.isInstance(error)) continue
            for (handler in handlers) {
                try {
                    handler(error)
                } catch (error: Throwable) {
                    throw DropErrorHandlerException("Error handler did not complete", error)
                }
            }
        }
    }

    /**
     * Drops this property or raises an error if it was already dropped.
     *
     * **This is used by the RAkII compiler plugin to build the main drop chain
     * by inserting calls into the [Drop.drop] function.**
     */
    fun drop() {
        if (!_isDropped.compareAndSet(expectedValue = false, newValue = true)) {
            return // Return early and ignore any additional drops
        }
        // Release the reference we're holding to allow GC to clean up
        _value.exchange(null)?.let { value ->
            try {
                dropHandler(value)
            } catch (error: Throwable) {
                throw DropException("Drop handler did not complete", error)
            }
        }
    }

    /**
     * Creates a copy of this drop delegate using the same
     * owner instance, drop handler and initializer.
     * This leaves the internal value of the newly created
     * delegate uninitialized.
     *
     * @return A new drop delegate with the same owner instance,
     *  drop handler and initializer as this delegate.
     */
    fun copyWithoutValue(): DropDelegate<TYPE, OWNER> = DropDelegate(instance, dropHandler, initializer).apply {
        dropChain += this@DropDelegate.dropChain
        errorHandlers += this@DropDelegate.errorHandlers
    }

    /**
     * Creates a copy of this drop delegate using the same
     * owner instance, drop handler and initializer.
     * This also copies over the internal value of this delegate
     * if present, otherwise the value of the new delegate is
     * left uninitialized.
     *
     * @return A new drop delegate with the same owner instance,
     *  drop handler, initializer and value (if present) as this delegate.
     */
    fun copyWithValue(): DropDelegate<TYPE, OWNER> = copyWithoutValue().apply {
        this@DropDelegate._value.load()?.let(_value::store)
    }

    /**
     * Drops the given property when the initialization of this property fails
     * with the specified exception type [X].
     *
     * @param property The property to drop when the initialization of this property fails
     *  with the given exception type [X].
     * @param X The type of exception which causes the given property to be dropped.
     * @return This property.
     */
    inline fun <reified X : Throwable> dropOnError(
        property: KProperty1<OWNER, AutoCloseable?>
    ): DropDelegate<TYPE, OWNER> {
        property.get(instance)?.let { value ->
            dropChain.getOrPut(X::class) { ArrayList() } += value
        }
        return this
    }

    /**
     * Drops the given property when the initialization of this property fails.
     *
     * @param property The property to drop when the initialization of this property fails.
     * @return This property.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun dropOnAnyError(
        property: KProperty1<OWNER, AutoCloseable?>
    ): DropDelegate<TYPE, OWNER> = dropOnError<Throwable>(property)

    /**
     * Invokes the given callback when an exception of type [X] occurs
     * during the initialization of this property.
     *
     * @param callback The callback to invoke when an exception of type [X]
     *  is thrown during property initialization.
     * @param X The type of exception which triggers the callback invocation.
     * @return This property.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified X : Throwable> onError(
        noinline callback: (X) -> Unit
    ): DropDelegate<TYPE, OWNER> {
        errorHandlers.getOrPut(X::class) { ArrayList() } += callback as (Throwable) -> Unit
        return this
    }

    /**
     * Invokes the given callback when an exception occurs during
     * the initialization of this property.
     *
     * @param callback The callback to invoke when an exception is thrown
     *  during property initialization.
     * @return This property.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun onAnyError(
        noinline callback: (Throwable) -> Unit
    ): DropDelegate<TYPE, OWNER> = onError<Throwable>(callback)

    /**
     * Defaults the property value to null when an exception occurs
     * during property initialization at the end of a property chain.
     *
     * @return A new [ValueDelegate] to provide the nullable result
     *  of the terminated property chain.
     */
    @Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
    inline fun nullOnError(): ValueDelegate<TYPE?> = ValueDelegate { thisRef, property ->
        return@ValueDelegate try {
            getValue(thisRef, property)
        } catch (error: Throwable) {
            null
        }
    }

    /**
     * Defaults the property value using the given initializer
     * when an exception occurs during property initialization.
     *
     * @param initializer The initializer to invoke for providing a
     *  non-null default value when an exception is thrown during
     *  property initialization.
     * @return A new [ValueDelegate] to provide the non-null result
     *  of the terminated property chain.
     */
    @Suppress("UNUSED_PARAMETER")
    inline fun defaultOnError(
        crossinline initializer: () -> TYPE
    ): ValueDelegate<TYPE> = ValueDelegate { thisRef, property ->
        return@ValueDelegate try {
            getValue(thisRef, property)
        } catch (error: Throwable) {
            initializer()
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): TYPE = value
}