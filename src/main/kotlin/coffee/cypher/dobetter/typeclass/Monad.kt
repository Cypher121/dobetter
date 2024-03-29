package coffee.cypher.dobetter.typeclass

public interface Monad<out T> : Applicative<T> {
    public interface Type : Applicative.Type {
        override fun <T> pure(v: T): Monad<T>

        public fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): Monad<U>

        override fun <T, U, V> liftA2(f: (T, U) -> V, a: Applicative<T>, b: Applicative<U>): Monad<V> {
            return bind(a.downcast<T, Monad<T>>()) { t ->
                bind(b.downcast<U, Monad<U>>()) { u ->
                    pure(f(t, u))
                }
            }
        }

        override fun <T, U> fmap(f: (T) -> U, a: Functor<T>): Monad<U> = super.fmap(f, a).downcast()
    }
}
