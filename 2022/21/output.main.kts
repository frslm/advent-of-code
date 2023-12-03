import java.io.File

typealias Lookup = Map<String, Result>

sealed class Result {
    abstract fun evaluate(lookup: Lookup): Long
}

class Expression(val first: String, val operate: Long.(Long) -> Long, val second: String) : Result() {
    override fun evaluate(lookup: Lookup): Long =
        lookup[first]!!.evaluate(lookup).operate(lookup[second]!!.evaluate(lookup))
}

class Number(private val number: Long) : Result() {
    override fun evaluate(lookup: Lookup): Long = number
}

val plus: Long.(Long) -> Long = Long::plus
val minus: Long.(Long) -> Long = Long::minus
val times: Long.(Long) -> Long = Long::times
val div: Long.(Long) -> Long = Long::div

// Part 1:

// In a map, associate each monkey's name with a number or operation. Then for any monkey, we can recursively
// evaluate the result we're looking for. Another approach (to avoid arbitrarily deep stacks) is to repetitively
// replace a monkey's name with a value, and to keep repeating this until only root remains. But we can go for the
// first approach since it's more elegant.

val lookup = File("input.txt").readLines().associate { line ->
    val (name, job) = line.split(": ")
    val value = job.toLongOrNull()

    name to (value?.let {
        Number(it)
    } ?: job.split(" ").let {
        Expression(
            it[0],
            when (val operate = it[1]) {
                "+" -> plus
                "-" -> minus
                "*" -> times
                "/" -> div
                else -> throw Exception("Unknown operation `$operate`!")
            }, it[2]
        )
    })
}

println(lookup["root"]!!.evaluate(lookup))

// Part 2:

// Brute force approach is to keep trying numbers until the equality check passes. A smarter approach is to evaluate
// the fully known branch using Part 1's technique, but then to work backwards for the partially known branch to
// arrive at the number we need. Since the lookup table acts like a tree, we effectively want to invert one half
// of the lookup table so that the root node is actually humn now, then we can evaluate it as in Part 1.
//
// So how to implement this inversion? Can first start by marking all nodes that stretch from humn up to root. Then
// we know that the non-marked child of root is the number we want to reach by tuning humn. So looking at the marked
// child of root now, we swap it with its marked child by storing the inverted operation in an inverted lookup.
// We repeat this for each marked descendant, and stop after processing humn. Finally, we can copy over all
// relations from the original lookup that don't already exist in the inverted lookup.
//
// Now we can start at humn and evaluate it as in Part 1 but using the inverted lookup.

// Find all the nodes that lead to humn:
fun Lookup.nodesAlong(from: String, to: String): Set<String> =
    when (from) {
        to -> setOf(from)
        else -> when (val node = this[from]!!) {
            is Number -> setOf()
            is Expression -> (this.nodesAlong(node.first, to) + this.nodesAlong(node.second, to)).let {
                it + if (it.isNotEmpty()) setOf(from) else setOf()
            }
            else -> throw Exception("Don't know how to handle $node!")
        }
    }

val nodesToHumn = lookup.nodesAlong("root", "humn")

// Start off the inverted lookup using the root's children:
var parent = "root"
val invertedLookup = mutableMapOf<String, Result>(
    with(lookup[parent] as Expression) {
        if (first in nodesToHumn) {
            first to second
        } else {
            second to first
        }.let { (toInvert, regular) ->
            assert(toInvert in nodesToHumn)
            assert(regular !in nodesToHumn)

            parent = toInvert
            toInvert to Number(lookup[regular]!!.evaluate(lookup))
        }
    }
)

// Build the inverted lookup entries:
while (parent != "humn") {
    with(lookup[parent] as Expression) {
        if (first in nodesToHumn) {
            first to second
        } else {
            second to first
        }.let { (toInvert, regular) ->
            assert(toInvert in nodesToHumn)
            assert(regular !in nodesToHumn)

            invertedLookup[toInvert] = when (operate) {
                plus -> Expression(parent, minus, regular)
                times -> Expression(parent, div, regular)
                minus -> when (toInvert) {
                    first -> Expression(parent, plus, regular)
                    second -> Expression(regular, minus, parent)
                    else -> throw Exception("Node to invert must be a child!")
                }

                div -> when (toInvert) {
                    first -> Expression(parent, times, regular)
                    second -> Expression(regular, div, parent)
                    else -> throw Exception("Node to invert must be a child!")
                }

                else -> throw Exception("Don't know how to invert this operation!")
            }

            parent = toInvert
        }
    }
}

// Copy over any missing lookup entries:
lookup.forEach { (string, result) -> if (string !in invertedLookup) invertedLookup[string] = result }

// Evaluate from humn:
println(invertedLookup["humn"]!!.evaluate(invertedLookup))
