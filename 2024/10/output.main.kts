import java.io.File

val grid = File("input.txt").readLines().map { line -> line.map { it.digitToInt() } }

typealias Point = Pair<Int, Int>
typealias Vector = Pair<Int, Int>

fun Point.within(grid: List<List<Int>>): Boolean = first >= 0 && first < grid.size && second >= 0 && second < grid.first().size

operator fun Point.plus(vector: Vector) = first + vector.first to second + vector.second

operator fun List<List<Int>>.get(point: Point): Int = this[point.first][point.second]

fun List<List<Int>>.reachableFrom(start: Point, target: Int): List<Point> =
    if (this[start] == target) {
        listOf(start)
    } else {
        listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1).flatMap { direction ->
            (start + direction).let {
                if (it.within(this) && this[it] == this[start] + 1) {
                    this.reachableFrom(it, target)
                } else {
                    emptyList()
                }
            }
        }
    }

fun <T : Any> List<List<Int>>.mapPerTrailhead(operate: (Point) -> T): List<T> =
    this.flatMapIndexed { row, line ->
        line.mapIndexed { col, height ->
            if (height == 0) { operate(row to col) } else { null }
        }.filterNotNull()
    }

// Part 1:
println(
    grid.mapPerTrailhead { grid.reachableFrom(it, 9).toSet().size }.sum()
)

// Part 2:
println(
    grid.mapPerTrailhead { grid.reachableFrom(it, 9).size }.sum()
)
