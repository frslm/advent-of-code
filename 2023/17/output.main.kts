import java.io.File
import kotlin.math.abs

enum class Direction {
    N, S, W, E;
    val opposite get() = when (this) { N -> S; S -> N; W -> E; E -> W }
}

data class Block(
    val heatLoss: Int,
    val minHeatLossFollowingPath: MutableMap<List<Direction>, Int> = mutableMapOf()
)

typealias Row = Int; typealias Col = Int
data class Location(val row: Row, val col: Col) {
    fun oneStepTowards(direction: Direction) = when (direction) {
        Direction.N -> Location(row - 1, col)
        Direction.S -> Location(row + 1, col)
        Direction.W -> Location(row, col - 1)
        Direction.E -> Location(row, col + 1)
    }

    fun manhattanDistanceTo(other: Location) = abs(row - other.row) + abs(col - other.col)
}

operator fun <T> List<List<T>>.get(location: Location) = this[location.row][location.col]

fun <T> List<List<T>>.getNeighbours(location: Location): Map<Direction, Location?> =
    listOf(
        Direction.N to if (location.row - 1 >= 0) Location(location.row - 1, location.col) else null,
        Direction.S to if (location.row + 1 < this.size) Location(location.row + 1, location.col) else null,
        Direction.W to if (location.col - 1 >= 0) Location(location.row, location.col - 1) else null,
        Direction.E to if (location.col + 1 < this.first().size) Location(location.row, location.col + 1) else null
    ).toMap()

fun List<List<Block>>.buildMinHeatLossPathsTowards(source: Location, destination: Location) {
    this[destination].let { it.minHeatLossFollowingPath[emptyList()] = it.heatLoss }

    this.traverseIntoNeighbours(destination, source, visited = setOf(destination))
}

fun List<List<Block>>.traverseIntoNeighbours(
    location: Location,
    towards: Location,
    visited: Set<Location>
) = this.getNeighbours(location)
    .mapNotNull { (direction, location) -> location?.let { direction to it } }
    .sortedBy { (_, location) -> location.manhattanDistanceTo(towards) }
    .forEach { (direction, location) -> this.traverse(location, towards, direction = direction.opposite, visited) }

fun <T : Any> List<T>.takeStreak(): List<T> =
    this.firstOrNull()?.let {
        var index = 1
        while (index < this.size && this[index] == it) index++
        this.take(index)
    } ?: emptyList()

var minHeatLossSoFar: Int? = null

fun List<List<Block>>.traverse(
    location: Location,
    towards: Location,
    direction: Direction,
    visited: Set<Location>
) {
    if (location in visited) return

    val block = this[location]

    val potentialPaths = this[location.oneStepTowards(direction)].minHeatLossFollowingPath.mapNotNull { (path, minHeatLoss) ->
        val newPath = listOf(direction) + path
        when {
            newPath[0] == newPath.getOrNull(1)?.opposite -> null
            // (can't reverse direction)

            newPath[0] == newPath.getOrNull(1) &&
            newPath[0] == newPath.getOrNull(2) &&
            newPath[0] == newPath.getOrNull(3) -> null
            // (can't move more than 3 blocks in the same direction)

            else -> newPath.takeStreak() to block.heatLoss + minHeatLoss
            // (only take the streaks to trim away unnecessary paths)
        }
    }

    if (potentialPaths.isNotEmpty()) {
        var freshPath = false
        potentialPaths.forEach { (path, minHeatLoss) ->
            block.minHeatLossFollowingPath[path].let {
                if (it == null || minHeatLoss < it) {
                    block.minHeatLossFollowingPath[path] = minHeatLoss
                    freshPath = true
                }
            }
        }

        minHeatLossSoFar.let {
            val min = block.minHeatLossFollowingPath.values.min()
            if (it == null || min < it) {
                if (location == towards) {
                    minHeatLossSoFar = min
                    println(minHeatLossSoFar) // (since this implementation is slow, this helps convey the progress)
                } else {
                    if (freshPath) {
                        this.traverseIntoNeighbours(location, towards, visited + location)
                    }
                }
            }
        }
    }
}

// Part 1:

// A standard approach is to work backwards from the destination and build the steadily-increasing heat loss cost into
// adjacent blocks until traversal naturally ends. But this is path-dependent, so need to include the path somehow.
// Can try building a map of paths and their minimum heat loss for each tile, with the following optimizations:
// - Trim parts of a path that don't contribute to a straight-line-streak.
// - Don't visit already-visited tiles (basic optimization from Dijkstra's).
// - Pick the neighbour closer to the source first when deciding where to traverse next (similar to A*'s heuristic).
// - Once a path to the source is found, along with its heat loss, exit early out of any paths that exceed this loss.
//
// But even with all those optimization attempts, this is still several minutes too slow, though it does solve it in
// the end. Maybe should start with an actual A* implementation first, and then figure out how to account for path
// dependence.

val source = Location(0, 0)

println(
    File("input.txt").readLines().map { row -> row.map { Block(it.digitToInt()) } }
        .also { it.buildMinHeatLossPathsTowards(source, Location(it.size - 1, it.first().size - 1)) }
        .let { it[source].minHeatLossFollowingPath.values.min() - it[source].heatLoss } // (source heat loss can be skipped)
)

// Part 2:

// (shouldn't even be attempted until an actual quick solution for Part 1 is implemented)

println(

)
