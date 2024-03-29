package coffee.cypher.dobetter.typeclass

interface Functor<out T> {
    interface Type {
        fun <T, U> fmap(f: (T) -> U, a: Functor<T>): Functor<U>
    }
}

@Suppress("UNCHECKED_CAST")
fun <T, A : Functor<T>> Functor<T>.downcast(): A = this as A