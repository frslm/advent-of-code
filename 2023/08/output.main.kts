import java.io.File

data class Node(val left: String, val right: String)

enum class Direction { L, R }

val lines = File("input.txt").readLines()

val instructions = lines[0].map { Direction.valueOf(it.toString()) }

val map = lines.subList(2, lines.size).associate { line ->
    Regex("([A-Z]{3}) = \\(([A-Z]{3}), ([A-Z]{3})\\)").matchEntire(line)!!.groups.let {
        it[1]!!.value to Node(it[2]!!.value, it[3]!!.value)
    }
}

fun String.stepsTo(instructions: List<Direction>, map: Map<String, Node>, isDestination: String.() -> Boolean): Long {
    var steps = 0L
    var node = this

    while (true) {
        instructions.forEach { direction ->
            when (direction) {
                Direction.L -> map[node]!!.left
                Direction.R -> map[node]!!.right
            }
                .also { steps += 1 }
                .also { if (it.isDestination()) { return steps } }
                .also { node = it }
        }
    }
}

// Part 1:

// Nothing special, just traverse the map:

println("AAA".stepsTo(instructions, map) { this == "ZZZ" } )

// Part 2:

// Just extend the stepper to carry a list of current nodes instead of only one single node at a time.
//
// Nevermind, traversing them all simultaneously takes too long. So instead, find the cycle length for each starting
// node separately, then find the lowest common multiple for all cycle lengths. This assumes that each starting point
// only ever has a single ending point that it cycles through, which is likely since "the number of nodes with names
// ending in A is equal to the number ending in Z".

fun Long.greatestCommonDivisor(other: Long): Long {
    var (lhs, rhs) = this to other

    while (rhs != 0L) {
        val temp = rhs
        rhs = lhs % rhs
        lhs = temp
    }

    return lhs
}

fun Long.lowestCommonMultiple(other: Long): Long =
    this * other / this.greatestCommonDivisor(other)

fun Char.simultaneousStepsTo(destinationEndingIn: Char, instructions: List<Direction>, map: Map<String, Node>): Long =
    map.keys
        .filter { it.endsWith(this) }
        .map { it.stepsTo(instructions, map) { this.endsWith(destinationEndingIn) } }
        .reduce { acc, steps -> acc.lowestCommonMultiple(steps) }

println('A'.simultaneousStepsTo('Z', instructions, map))
