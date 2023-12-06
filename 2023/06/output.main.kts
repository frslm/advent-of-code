import java.io.File

val lines = File("input.txt").readLines()

// Part 1:

// Basically multiplying the sides of a rectangle; the more square-like, the larger the product. Also, it's symmetric
// beyond the midway point, so can stop halfway then double the results if we need a small optimization.

fun String.getNumbersFor(header: String): List<Int> =
    Regex("$header: (.*)").matchEntire(this)!!.groups[1]!!.value
        .split(" ").filter { it.isNotEmpty() }.map { it.toInt() }

println(
    lines
        .let { it[0].getNumbersFor("Time").zip(it[1].getNumbersFor("Distance")) }
        .map { (time, distance) -> (0 .. time).count { it * (time - it) > distance } }
        .reduce { acc, product -> acc * product }
)

// Part 2:

// Didn't need to overthink this in the end, just use the exact same approach. Was considering using a binary search to
// find the boundary between a record-beating and record-losing run, counting its distance from the peak (found
// by taking the square root then getting the closest matching integers), then doubling the result since it's symmetric,
// taking care to handle an even time a bit differently from an odd run.

fun String.getJoinedNumberFor(header: String): Long =
    Regex("$header: (.*)").matchEntire(this)!!.groups[1]!!.value
        .split(" ").filter { it.isNotEmpty() }.joinToString("").toLong()

println(
    lines
        .let { it[0].getJoinedNumberFor("Time") to it[1].getJoinedNumberFor("Distance") }
        .let { (time, distance) -> (0 .. time).count { it * (time - it) > distance } }
)
