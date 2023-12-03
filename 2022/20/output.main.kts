import java.io.File

val lines = File("input.txt").readLines()

fun MutableList<Pair<Int, Long>>.mix() {
    for (i in this.indices) {
        val index = this.indexOfFirst { (order, _) -> order == i }
        val element = this[index]

        this.removeAt(index)
        this.add(Math.floorMod(element.second + index, this.size), element) // (floorMod to avoid negative modulo)
    }
}

// Part 1:

// One approach is to attach the original order to each number, then loop through the entire list for each item
// and move it accordingly. The issue here is us needing to re-loop through the entire list to find the next item
// by order. Could store the indices of each element in a separate list, but again that needs to be updated whenever
// any element moves, and in that case, while the lookup is O(1), the act of updating all in-between element indices
// is still O(N). For Part 1, let's just go with the O(N) linear-search-on-each-iteration approach, since it should
// be enough.

with(lines.mapIndexed { order, number -> order to number.toLong() }.toMutableList()) {
    this.mix()

    val index = this.indexOfFirst { (_, number) -> number == 0L }
    println(listOf(1000, 2000, 3000).sumOf { this[(index + it) % this.size].second })
}

// Part 2:

// Good, part two just mutates the numbers without substantially increasing the number of mixes required, so can
// just apply the mutation and proceed as normal.

with(lines.mapIndexed { order, number -> order to number.toLong() * 811589153L }
    .toMutableList()) {
    repeat(10) { this.mix() }

    val index = this.indexOfFirst { (_, number) -> number == 0L }
    println(listOf(1000, 2000, 3000).sumOf { this[(index + it) % this.size].second })
}
