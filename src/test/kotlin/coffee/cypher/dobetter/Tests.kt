package coffee.cypher.dobetter

import coffee.cypher.dobetter.type.*
import kotlin.test.Test

class Tests {
    @Test fun maybeTest() {
        val a = maybe {
            val x = Maybe.Some(1).bind()
            val y = Maybe.Some(2).bind()

            x + y
        }

        require(a == Maybe.Some(3))

        val b = Maybe.Type.evaluate {
            val x = Maybe.Some(1).bind()
            val y = Maybe.None.bind<Int>()

            x + y
        }

        require(b == Maybe.None)
    }

    @Test fun stateTest() {
        val readChar = State<String, Char> { it.drop(1) to it.first() }

        val c = state<String, String> {
            List(3) {
                readChar.bind()
            }.joinToString()
        }

        val (rem, res) = c.run("abcd")
        require(rem == "d")
        require(res == "a, b, c")
    }

    @Test fun listTest() {
        val d = list {
            val x = listOf(1, 2, 3).m.bind()

            x * 2
        }

        require(d == listOf(2, 4, 6))

        fun <T> dup(list: List<T>): List<T> {
            return list {
                val x = list.m.bind()

                listOf(x, x).m.bind()
            }
        }

        require(dup(listOf(1, 2, 3)) == listOf(1, 1, 2, 2, 3, 3))

        fun <T> cross(a: List<T>, b: List<T>) = list {
            a.m.bind() to b.m.bind()
        }

        require(
            cross(
                listOf(1, 2),
                listOf(3, 4),
            ) == listOf(
                1 to 3,
                1 to 4,
                2 to 3,
                2 to 4,
            )
        )
    }

    @Test fun contTest() {
        val helloCont = Cont<Unit, String> {
            it("Hello")
        }

        val x = cont<List<Char>, List<Char>> {
            val a = helloCont.typed<List<Char>, String>().bind()

            a.toList()
        }.runCont {
            it.distinct()
        }

        require(x == "Helo".toList())
    }
}