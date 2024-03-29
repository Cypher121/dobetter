package coffee.cypher.dobetter.typeclass

public interface Functor<out T> {
    public interface Type {
        public fun <T, U> fmap(f: (T) -> U, a: Functor<T>): Functor<U>
    }
}

@Suppress("UNCHECKED_CAST")
public fun <T, A : Functor<T>> Functor<T>.downcast(): A = this as A