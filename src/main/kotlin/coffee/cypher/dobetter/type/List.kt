package coffee.cypher.dobetter.type

import coffee.cypher.dobetter.ComputationContext
import coffee.cypher.dobetter.evaluate
import coffee.cypher.dobetter.typeclass.*

@JvmInline
public value class MList<T>(public val value: List<T>) : Monad<T> {
    public object Type : Monad.Type, ToParallel<ZipList.Type, Type> {
        override fun <T, U> bind(m: Monad<T>, f: (T) -> Monad<U>): MList<U> =
            m.downcast<T, MList<T>>()
                .value
                .flatMap {
                    f(it).downcast<U, MList<U>>().value
                }.let(::MList)

        override fun <T> pure(v: T): MList<T> =
            v.let(::listOf).let(::MList)

        override fun <T> parallel(m: Monad<T>): ZipList<T> =
            m.downcast<T, MList<T>>().value.let(::ZipList)

        override val parallelType: ZipList.Type = ZipList.Type
    }
}

@JvmInline
public value class ZipList<T>(public val value: List<T>) : Applicative<T> {
    public object Type : Applicative.Type, Parallel<MList.Type, Type> {
        override fun <T> pure(v: T): Applicative<T> =
            ZipList(listOf(v))

        override fun <T, U, V> liftA2(f: (T, U) -> V, a: Applicative<T>, b: Applicative<U>): ZipList<V> {
            val listA = a.downcast<T, ZipList<T>>().value
            val listB = b.downcast<U, ZipList<U>>().value

            return listA.zip(listB, f).let(::ZipList)
        }

        override fun <T> sequence(m: Applicative<T>): MList<T> =
            m.downcast<T, ZipList<T>>().value.let(::MList)
    }
}

public fun <T> list(f: suspend ComputationContext<T, MList.Type>.() -> T): List<T> =
    MList.Type.evaluate<T, MList<T>, MList.Type>(f).value

context(ComputationContext<*, MList.Type>)
public inline val <T> List<T>.m: MList<T>
    get() = let(::MList)