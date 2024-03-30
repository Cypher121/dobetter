package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast

public sealed interface Maybe<out T> : Monad<T> {
    public data class Some<T>(val value: T) : Maybe<T>
    public data object None : Maybe<Nothing>

    public object Type : Monad.Type {
        override fun <T> pure(v: T): Maybe<T> = Some(v)

        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): Maybe<U> =
            when (val maybe = m.downcast<T, Maybe<T>>()) {
                is Some<T> -> f(maybe.value).downcast()
                is None -> None
            }
    }

    public companion object {
        public fun <T> of(value: T): Maybe<T> =
            Type.pure(value)
        public fun <T> ofNullable(value: T): Maybe<T & Any> =
            value?.let(::Some) ?: None
    }
}

public fun <T> Maybe<T>.getOrThrow(): T =
    downcast<T, Maybe.Some<T>>().value

public fun <T : Any> maybe(f: suspend ComputationContext<T, Maybe.Type>.() -> T): Maybe<T> =
    Maybe.Type.evaluate(f)