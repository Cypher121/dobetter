package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun interface Cont<R, A> : Monad<A> {
    public fun runCont(f: (A) -> R): R

    public class Type<R> : Monad.Type {
        override fun <T> pure(v: T): Cont<R, T> = Cont{
            it(v)
        }

        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): Cont<R, U> =
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

public fun <R, T> Cont<Unit, T>.typed(): Cont<R, T> = Cont { f ->
    var result: R

    runContInPlace {
        result = f(it)
    }

    result
}

public fun <R, T> cont(f: suspend ComputationContext<T, Cont.Type<R>>.() -> T): Cont<R, T> =
    Cont.Type<R>().evaluate(f)