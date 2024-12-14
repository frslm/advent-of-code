import java.io.File

val lines = File("input.txt").readLines()

typealias Vector = Pair<Int, Int>

val Vector.inGrid get(): Boolean = first >= 0 && first < lines.size && second >= 0 && second < lines.first().length

operator fun Vector.plus(vector: Vector): Vector = first + vector.first to second + vector.second
operator fun Vector.minus(vector: Vector): Vector = first - vector.first to second - vector.second

val antennas = mutableMapOf<Char, MutableList<Vector>>().apply {
    lines.forEachIndexed { row, line ->
        line.forEachIndexed { col, char ->
            if (char != '.') {
                getOrPut(char) { mutableListOf() }.add(row to col)
            }
        }
    }
}

fun Map<Char, List<Vector>>.countAntinodesUsing(calculate: Pair<Vector, Vector>.() -> List<Vector>) =
    this.values.flatMap { locations ->
        (0 until locations.size - 1).flatMap { left ->
            (left + 1 until locations.size).flatMap { right ->
                (locations[left] to locations[right]).calculate()
            }
        }
    }.toSet().size

// Part 1:
fun Pair<Vector, Vector>.calculateAntinodePair(): List<Vector> =
    (this.first - this.second).let {
        listOf(
            this.first + it,
            this.second - it
        )
    }.filter { it.inGrid }

println(
    antennas.countAntinodesUsing { calculateAntinodePair() }
)

// Part 2:
fun Vector.projectLineAlong(vector: Vector): List<Vector> =
    mutableListOf<Vector>().also {
        var location = this
        while (location.inGrid) {
            it.add(location)
            location += vector
        }
    }

fun Pair<Vector, Vector>.calculateAntinodeLine(): List<Vector> =
    first.projectLineAlong(this.first - this.second) +
    second.projectLineAlong(this.second - this.first)

println(
    antennas.countAntinodesUsing { calculateAntinodeLine() }
)
