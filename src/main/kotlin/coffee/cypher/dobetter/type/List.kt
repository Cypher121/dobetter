package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast


data class MList<T>(val value: List<T>) : Monad<T> {
    object Type : Monad.Type {
        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>) =
            m.downcast<T, MList<T>>()
                .value
                .flatMap {
                    f(it).downcast<U, MList<U>>().value
                }.m

        override fun <T> pure(v: T) =
            v.let(::listOf).m
    }
}

fun <T> list(f: suspend ComputationContext<T>.() -> T): List<T> = MList.Type.evaluate<T, MList<T>>(f).value

val <T> List<T>.m get() = let(::MList)