package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast

sealed interface Maybe<out T : Any> : Monad<T> {
    data class Some<T : Any>(val value: T) : Maybe<T>
    data object None : Maybe<Nothing>

    object Type : Monad.Type {
        override fun <T> pure(v: T): Maybe<T & Any> = v?.let { Some(it) } ?: None

        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>) =
            when (val maybe = m.downcast<T, Maybe<T & Any>>()) {
                is Some<T & Any> -> f(maybe.value)
                is None -> None
            }
    }
}

fun <T : Any> maybe(f: suspend ComputationContext<T>.() -> T): Maybe<T> = Maybe.Type.evaluate(f)