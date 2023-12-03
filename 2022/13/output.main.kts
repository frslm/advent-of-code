import java.io.File
import kotlin.math.max

typealias Index = Int

fun String.closingBracket(index: Index): Index {
    assert(this[index] == '[')

    var depth = 0
    for (i in index until this.length) {
        when (this[i]) {
            '[' -> depth += 1
            ']' -> depth -= 1
        }

        if (depth == 0) return i
    }

    throw Exception("Unbalanced string?")
}

fun String.finalDigit(index: Index): Index {
    assert(this[index].isDigit())

    for (i in index until this.length) {
        if (!this[i].isDigit()) return i - 1
    }

    return this.length - 1
}

fun String.parse(): List<Any> {
    assert(this.first() == '[')
    assert(this.last() == ']')

    val contents = this.substring(1, this.length - 1)

    return mutableListOf<Any>().also { chunks ->
        var i = 0
        while (i < contents.length) {
            i = when (contents[i]) {
                '[' -> contents.closingBracket(i).also { chunks.add(contents.substring(i, it + 1).parse()) }
                else -> contents.finalDigit(i).also { chunks.add(contents.substring(i, it + 1).toInt()) }
            } + 2
        }
    }
}

enum class Order { Correct, Incorrect, Identical }

@Suppress("UNCHECKED_CAST")
fun Pair<List<Any>, List<Any>>.order(): Order {
    (0 until max(this.first.size, this.second.size)).forEach {
        when {
            it >= this.first.size -> Order.Correct
            it >= this.second.size -> Order.Incorrect
            else -> {
                val left = this.first[it]
                val right = this.second[it]

                if (left is Int && right is Int) {
                    when {
                        left < right -> Order.Correct
                        left > right -> Order.Incorrect
                        else -> Order.Identical
                    }
                } else if (left is List<*> && right is List<*>) {
                    Pair(left as List<Any>, right as List<Any>).order()
                } else {
                    if (left is Int) {
                        assert(right is List<*>)
                        Pair(listOf(left), right as List<Any>).order()
                    } else {
                        assert(right is Int)
                        assert(left is List<*>)
                        Pair(left as List<Any>, listOf(right)).order()
                    }
                }
            }
        }.let { order -> if (order != Order.Identical) return order }
    }

    return Order.Identical
}

val packetPairLength = 3

val lines = File("input.txt").readLines()

// Part 1:
println(
    (0 until (lines.size + 1) / packetPairLength)
        .map { Pair(lines[packetPairLength * it + 0].parse(), lines[packetPairLength * it + 1].parse()) }
        .mapIndexed { i, pair -> Pair(i + 1, pair) }
        .filter { it.second.order() == Order.Correct }
        .sumOf { it.first }
)

// Part 2:
val dividerPackets = listOf(listOf(listOf(2)), listOf(listOf(6)))
println(
    (lines.mapNotNull { if (it.isNotEmpty()) it.parse() else null } + dividerPackets)
        .asSequence()
        .sortedWith { left, right ->
            when (Pair(left, right).order()) {
                Order.Correct -> -1
                Order.Incorrect -> 1
                Order.Identical -> 0
            }
        }
        .mapIndexed { i, packet -> Pair(i + 1, packet) }
        .filter { dividerPackets.any { divider -> Pair(divider, it.second).order() == Order.Identical } }
        .map { it.first }
        .reduce { acc, index -> acc * index }
)
