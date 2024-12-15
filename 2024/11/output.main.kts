import java.io.File

val stones: List<Stone> = File("input.txt").readText().split(' ')

typealias Stone = String
typealias Count = Long

fun Stone.blink(): List<Stone> =
    when {
        this == "0" -> listOf("1")
        this.length % 2 == 0 -> listOf(
            this.take(this.length / 2).toLong().toString(),
            this.takeLast(this.length / 2).toLong().toString()
        )
        else -> listOf((this.toLong() * 2024L).toString())
    }

// Part 1:
fun List<Stone>.blink(): List<Stone> = this.flatMap { it.blink() }

println(
    stones.let {
        var arrangement = it
        repeat(25) { arrangement = arrangement.blink() }
        arrangement.size
    }
)

// Part 2:

// Longer simulation as above stops working due to limited heap size and simulation taking exponentially longer per
// stage. One optimization is to group identical stones in the same bucket along with their count. That way, each stage
// performs only a single operation per type of stone.

fun Map<Stone, Count>.blink(): Map<Stone, Count> =
    mutableMapOf<Stone, Count>().also {
        this.forEach { (stone, count) ->
            stone.blink().forEach { newStone ->
                it[newStone] = it[newStone]?.let { it + count } ?: count
            }
        }
    }

println(
    stones.groupingBy { it }.eachCount().mapValues { (stone, count) -> count.toLong() }.let {
        var arrangement = it
        repeat(75) { arrangement = arrangement.blink() }
        arrangement.map { (stone, count) -> count }.sum()
    }
)
