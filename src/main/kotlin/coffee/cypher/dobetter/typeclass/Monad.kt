package coffee.cypher.dobetter.typeclass

interface Monad<out T> : Applicative<T> {
    interface Type : Applicative.Type {
        override fun <T> pure(v: T): Monad<T>

        fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): Monad<U>

        override fun <T, U, V> liftA2(f: (T, U) -> V, a: Applicative<T>, b: Applicative<U>): Applicative<V> {
            return bind(a as Monad<T>) { t ->
                bind(b as Monad<U>) { u ->
                    pure(f(t, u))
                }
            }
        }
    }
}
