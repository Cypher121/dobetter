package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast

public fun interface State<S, A> : Monad<A> {
    public fun run(state: S): Pair<S, A>

    public class Type<S> : Monad.Type {
        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): State<S, U> =
            State {
                val (newS, a) = m.downcast<T, State<S, T>>().run(it)
                f(a).downcast<U, State<S, U>>().run(newS)
            }

        override fun <U> pure(v: U): State<S, U> =
            State {
                it to v
            }
    }
}

public fun <S, T> state(f: suspend ComputationContext<T, State.Type<S>>.() -> T): State<S, T> =
    State.Type<S>().evaluate(f)
