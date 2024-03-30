package coffee.cypher.dobetter

import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast
import kotlin.coroutines.*

@RestrictsSuspension
public class ComputationContext<T, MT : Monad.Type>(internal val type: MT) {
    private lateinit var result: Monad<T>

    public suspend fun <U> Monad<U>.bind(): U {
        return suspendCoroutine { cont ->
            val unsafe = cont.toUnsafe()

            result = type.bind(this) { handleBind(clone(unsafe), it) }
        }
    }

    public suspend fun <U, V> Pair<Monad<U>, Monad<V>>.bind(): Pair<U, V> =
        type.liftA2(::Pair, first, second).bind()

    private fun handleDone(value: T) {
        result = type.pure(value)
    }

    private fun <U> handleBind(cont: Continuation<U>, value: U): Monad<T> {
        cont.resume(value)

        return result
    }

    internal fun evaluate(f: suspend ComputationContext<T, MT>.() -> T): Monad<T> {
        f.startCoroutine(this, object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                handleDone(result.getOrThrow())
            }
        })

        return result
    }
}

public fun <T, M : Monad<T>, MT : Monad.Type> MT.evaluate(f: suspend ComputationContext<T, MT>.() -> T): M =
    ComputationContext<T, MT>(this).evaluate(f).downcast()