package coffee.cypher.dobetter

import coffee.cypher.dobetter.type.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Tests {
    @Test
    fun maybeTest() {
        val a = maybe {
            val x = Maybe.of(1).bind()
            val y = Maybe.of(2).bind()

            x + y
        }

        assertEquals(Maybe.of(3), a)

        val b = Maybe.Type.evaluate<Int, Maybe<Int>, Maybe.Type> {
            val x = Maybe.of(1).bind()
            val y = Maybe.ofNullable(null as Int?).bind()

            x + y
        }

        assertTrue(b.isNone)
    }

    @Test
    fun stateTest() {
        val readChar = State<String, Char> { it.drop(1) to it.first() }

        val c = state<String, String> {
            List(3) {
                readChar.bind()
            }.joinToString()
        }

        val (rem, res) = c.run("abcd")
        assertEquals("d", rem)
        assertEquals("a, b, c", res)
    }

    @Test
    fun listTest() {
        val d = list {
            val x = listOf(1, 2, 3).m.bind()

            x * 2
        }

        assertEquals(listOf(2, 4, 6), d)

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

        assertEquals(
            listOf(
                1 to 3,
                1 to 4,
                2 to 3,
                2 to 4,
            ),
            cross(
                listOf(1, 2),
                listOf(3, 4),
            )
        )

        fun <T, U> zip(a: List<T>, b: List<U>): List<Pair<T, U>> {
            return list {
                (a.m to b.m).parallel().bind()
            }
        }

        assertEquals(
            listOf(
                1 to 3,
                2 to 4,
            ),
            zip(
                listOf(1, 2),
                listOf(3, 4),
            )
        )
    }

    @Test
    fun contTest() {
        val helloCont = Cont<Unit, String> {
            it("Hello")
        }

        val x = cont<List<Char>, List<Char>> {
            val a = helloCont.typed<List<Char>, String>().bind()

            a.toList()
        }.runCont {
            it.distinct()
        }

        assertEquals("Helo".toList(), x)
    }
}