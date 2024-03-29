package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun interface Cont<R, A> : Monad<A> {
    fun runCont(f: (A) -> R): R

    class Type<R> : Monad.Type {
        override fun <T> pure(v: T) = Cont<R, T> {
            it(v)
        }

        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>) =
            Cont {
                m.downcast<T, Cont<R, T>>()
                    .runCont { t ->
                        f(t).downcast<U, Cont<R, U>>().runCont(it)
                    }
            }
    }
}

@OptIn(ExperimentalContracts::class)
private fun <R, A> Cont<R, A>.runContInPlace(f: (A) -> R): R {
    contract {
        callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }

    return runCont(f)
}

fun <R, T> Cont<Unit, T>.typed(): Cont<R, T> = Cont { f ->
    var result: R

    runContInPlace {
        result = f(it)
    }

    result
}

fun <R, T> cont(f: suspend ComputationContext<T>.() -> T): Cont<R, T> = Cont.Type<R>().evaluate(f)