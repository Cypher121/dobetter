package coffee.cypher.dobetter

import java.lang.reflect.Modifier
import kotlin.coroutines.Continuation

private val UNSAFE = Class.forName("sun.misc.Unsafe")
    .getDeclaredField("theUnsafe")
    .apply { isAccessible = true }
    .get(null) as sun.misc.Unsafe

@Suppress("UNCHECKED_CAST")
fun <T : Any> clone(obj: T): T {
    val clazz = obj::class.java
    val copy = UNSAFE.allocateInstance(clazz) as T
    copyDeclaredFields(obj, copy, clazz)
    return copy
}

private tailrec fun <T> copyDeclaredFields(obj: T, copy: T, clazz: Class<out T>) {
    for (field in clazz.declaredFields) {
        if (Modifier.isStatic(field.modifiers)) {
            continue
        }

        field.isAccessible = true
        val v = field.get(obj)
        field.set(copy, if (v === obj) copy else v)
    }
    val superclass = clazz.superclass
    if (superclass != null) copyDeclaredFields(obj, copy, superclass)
}

@Suppress("UNCHECKED_CAST")
private val safeContinuationClass: Class<Continuation<*>> =
    Class.forName("kotlin.coroutines.SafeContinuation") as Class<Continuation<*>>

private val safeContinuationDelegateField = safeContinuationClass.getDeclaredField("delegate").also {
    it.isAccessible = true
}

tailrec fun <T> Continuation<T>.toUnsafe(): Continuation<T> {
    return if (safeContinuationClass.isInstance(this)) {
        @Suppress("UNCHECKED_CAST")
        (safeContinuationDelegateField.get(this) as Continuation<T>).toUnsafe()
    } else {
        this
    }
}