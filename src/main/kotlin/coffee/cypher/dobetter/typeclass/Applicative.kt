package coffee.cypher.dobetter.typeclass

public interface Applicative<out T> : Functor<T> {
    public interface Type : Functor.Type {
        public fun <T> pure(v: T): Applicative<T>

        public fun <T, U, V> liftA2(f: (T, U) -> V, a: Applicative<T>, b: Applicative<U>): Applicative<V>

        public fun <T, U> sequence(f: Applicative<(T) -> U>, a: Applicative<T>): Applicative<U> {
            return liftA2({ x, y -> x(y) }, f, a)
        }

        override fun <T, U> fmap(f: (T) -> U, a: Functor<T>): Applicative<U> =
            sequence(pure(f), a.downcast())
    }
}
