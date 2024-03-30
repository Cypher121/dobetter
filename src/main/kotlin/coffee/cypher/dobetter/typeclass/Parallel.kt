package coffee.cypher.dobetter.typeclass

public interface ToParallel<S, P>
        where S : Parallel<P, S>, S : Applicative.Type,
              P : ToParallel<S, P>, P : Monad.Type {
    public fun <T> parallel(m: Monad<T>): Applicative<T>

    public val parallelType: S
}

public interface Parallel<P, S>
        where P : ToParallel<S, P>, P : Monad.Type,
              S : Parallel<P, S>, S : Applicative.Type {
    public fun <T> sequence(m: Applicative<T>): Monad<T>
}