import java.io.File

val lines = File("input.txt").readLines()

typealias Vector = Pair<Int, Int>
fun <T> Vector.within(grid: List<List<T>>): Boolean = first >= 0 && first < grid.size && second >= 0 && second < grid.first().size
operator fun Vector.plus(vector: Vector): Vector = first + vector.first to second + vector.second
operator fun Vector.minus(vector: Vector): Vector = first - vector.first to second - vector.second
operator fun <T> List<List<T>>.get(vector: Vector): T = this[vector.first][vector.second]

typealias Distance = Long

data class Tile(
    var symbol: Char,
    var distance: Distance? = null,
    var visited: Boolean = false
)

val GRID_SIZE = 71

fun generateGridWithNObstacles(n: Int): MutableList<MutableList<Tile>> {
    val grid = MutableList(GRID_SIZE) {
        MutableList(GRID_SIZE) {
            Tile('.')
        }
    }

    lines.take(n).forEach { line ->
        line.split(",").let { (first, second) ->
            grid[first.toInt() to second.toInt()].symbol = '#'
        }
    }

    return grid
}

// Part 1:
var shortestDistanceSoFar: Distance? = null

fun List<List<Tile>>.traverse(
    locationToDistanceSoFar: Pair<Vector, Distance>
): List<Pair<Vector, Distance>> {
    if (this[locationToDistanceSoFar.first].distance != null && this[locationToDistanceSoFar.first].distance != 0L) {
        return emptyList()
    } // (this short-circuit works with Dijkstra's wavefront heuristic below to quickly traverse the grid)

    shortestDistanceSoFar?.let {
        if (locationToDistanceSoFar.second > it) {
            return emptyList()
        }
    }

    this[locationToDistanceSoFar.first].distance?.let { shortestDistance ->
        if (locationToDistanceSoFar.second > shortestDistance) {
            return emptyList()
        }
    }

    this[locationToDistanceSoFar.first].distance.let { shortestDistance ->
        if (shortestDistance == null || locationToDistanceSoFar.second < shortestDistance) {
            this[locationToDistanceSoFar.first].distance = locationToDistanceSoFar.second
            if (locationToDistanceSoFar.first == end) {
                shortestDistanceSoFar.let {
                    if (it == null || locationToDistanceSoFar.second < it) {
                        shortestDistanceSoFar = locationToDistanceSoFar.second
                    }
                }
            }
        }
    }

    return listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        .map { locationToDistanceSoFar.first + it }
        .filter { it.within(this) }
        .filter { this[it].symbol != '#' }
        .map { it to locationToDistanceSoFar.second + 1L }
}

val start: Vector = 0 to 0
val end: Vector = GRID_SIZE - 1 to GRID_SIZE - 1

println(
    generateGridWithNObstacles(1024)
        .also { it[start].distance = 0 }
        .apply {
            val candidates = mutableListOf(start to 0L)

            var count = 0
            while (candidates.isNotEmpty()) {
                candidates.sortBy { it.second } // (Dijkstra's wavefront heuristic)

                candidates.addAll(0, traverse(candidates.take(1).single().also { candidates.removeAt(0) }))
                count++
            }
        }[end].distance
)

// Part 2:

// For some reason, the above traversal stops early on some rounds (probably cause it's a bootleg algorithm with
// Dijkstra's heuristic forced in there). Trying actual Dijkstra's instead for this part.

var n = 3000 // (start high then go low, since more obstacles speeds up traversal)
while (true) {
    val shortestDistance = generateGridWithNObstacles(n)
        .also { it[start].distance = 0 }
        .apply {
            while (true) {
                val unvisited = this
                    .flatMapIndexed { row, line -> line.mapIndexed { col, tile -> (row to col) to tile } }
                    .filter { !it.second.visited }

                if (unvisited.isEmpty()) break

                val unvisitedWithDistance = unvisited
                    .filter { it.second.distance != null }

                if (unvisitedWithDistance.isEmpty()) break

                val candidate = unvisitedWithDistance
                    .minBy { it.second.distance!! }

                listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                    .map { candidate.first + it }
                    .filter { it.within(this) }
                    .filter { this[it].symbol != '#' }
                    .forEach { neighbour ->
                        this[neighbour].distance.let {
                            if (it == null || it > candidate.second.distance!! + 1) {
                                this[neighbour].distance = candidate.second.distance
                            }
                        }
                    }

                candidate.second.visited = true
            }
        }[end].distance

    if (shortestDistance != null) {
        println(lines[n])
        break
    } else {
        n--
    }
}
