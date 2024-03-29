package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast

sealed interface Maybe<out T> : Monad<T> {
    data class Some<T>(val value: T) : Maybe<T>
    data object None : Maybe<Nothing>

    object Type : Monad.Type {
        override fun <T> pure(v: T): Maybe<T> = Some(v)

        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>) =
            when (val maybe = m.downcast<T, Maybe<T>>()) {
                is Some<T> -> f(maybe.value)
                is None -> None
            }
    }

    companion object {
        fun <T> of(value: T) = Type.pure(value)
        fun <T> ofNullable(value: T): Maybe<T & Any> = value?.let(::Some) ?: None
    }
}

fun <T> Maybe<T>.getOrThrow() = downcast<T, Maybe.Some<T>>().value

fun <T : Any> maybe(f: suspend ComputationContext<T>.() -> T): Maybe<T> = Maybe.Type.evaluate(f)