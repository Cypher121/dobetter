package coffee.cypher.dobetter

import coffee.cypher.dobetter.typeclass.Monad
import coffee.cypher.dobetter.typeclass.downcast
import kotlin.coroutines.*

@RestrictsSuspension
class ComputationContext<T>(private val type: Monad.Type) {
    private lateinit var result: Monad<T>

    suspend fun <U> Monad<U>.bind(): U {
        return suspendCoroutine { cont ->
            val unsafe = cont.toUnsafe()

            result = type.bind(this) { handleBind(clone(unsafe), it) }
        }
    }

    private fun handleDone(value: T) {
        result = type.pure(value)
    }

    private fun <U> handleBind(cont: Continuation<U>, value: U): Monad<T> {
        cont.resume(value)

        return result
    }

    internal fun evaluate(f: suspend ComputationContext<T>.() -> T): Monad<T> {
        f.startCoroutine(this, object : Continuation<T> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                handleDone(result.getOrThrow())
            }
        })

        return result
    }
}

fun <T, M : Monad<T>> Monad.Type.evaluate(f: suspend ComputationContext<T>.() -> T) =
    ComputationContext<T>(this).evaluate(f).downcast<T, M>()