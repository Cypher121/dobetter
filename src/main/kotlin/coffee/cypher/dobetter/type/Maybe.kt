package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast

@JvmInline
@Suppress("UNCHECKED_CAST")
public value class Maybe<out T>(private val value: Any?) : Monad<T> {
    public val isSome: Boolean get() = this != NONE
    public val isNone: Boolean get() = this == NONE

    public fun getOrThrow(): T =
        if (isSome) {
            value as T
        } else {
            throw NoSuchElementException()
        }

    public object Type : Monad.Type {
        override fun <T> pure(v: T): Maybe<T> = Maybe(v)

        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): Maybe<U> =
            m.downcast<T, Maybe<T>>().let { maybe ->
                if (maybe == NONE) {
                    NONE
                } else {
                    f(maybe.value as T).downcast()
                }
            }
    }

    public companion object {
        private val NONE: Maybe<Nothing> = Maybe(Any())

        public fun <T> of(value: T): Maybe<T> = Type.pure(value)

        public fun <T> ofNullable(value: T): Maybe<T & Any> = value?.let(::Maybe) ?: NONE

        public fun none(): Maybe<Nothing> = NONE
    }
}

public fun <T> Maybe<T>.getOrNull(): T? =
    if (isSome) {
        getOrThrow()
    } else {
        null
    }

public fun <T : Any> maybe(f: suspend ComputationContext<T, Maybe.Type>.() -> T): Maybe<T> =
    Maybe.Type.evaluate(f)