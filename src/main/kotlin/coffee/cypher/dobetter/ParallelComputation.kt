package coffee.cypher.dobetter

import coffee.cypher.dobetter.typeclass.Applicative
import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.Parallel
import coffee.cypher.dobetter.typeclass.ToParallel

context(ComputationContext<*, MT>)
public fun <T, U, MT, P> Pair<Monad<T>, Monad<U>>.parallel(): Monad<Pair<T, U>>
        where MT : Monad.Type, MT : ToParallel<P, MT>,
              P : Parallel<*, *>, P : Applicative.Type =
    type.parallelType.sequence(
        type.parallelType.liftA2(
            ::Pair,
            type.parallel(first),
            type.parallel(second)
        )
    )

context(ComputationContext<*, MT>)
public fun <T, MT, P> List<Monad<T>>.parallel(): Monad<List<T>>
        where MT : ToParallel<P, MT>, MT : Monad.Type,
              P : Parallel<*, *>, P : Applicative.Type =
    type.parallelType.sequence(
        fold(type.parallelType.pure(emptyList())) { acc, m ->
            type.parallelType.liftA2({ list, v -> list + v }, acc, type.parallel(m))
        }
    )