import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min

enum class Material { Air, Stone, Sand }

class Coordinates(val x: Int, val y: Int)

private typealias Grid = MutableList<MutableList<Material>>

fun buildGrid(): Grid {
    // Read raw input into data structure:
    val input = File("input.txt").readLines().map { line ->
        line.split(" -> ").map { coordinate ->
            coordinate.split(",").let { Coordinates(it[0].toInt(), it[1].toInt()) }
        }
    }

    // Build grid bounds:
    val largest = { getCoordinate: Coordinates.() -> Int ->
        input.mapNotNull { shape -> shape.maxOfOrNull { it.getCoordinate() } }.maxOrNull()
            ?: throw Exception("No coordinates exist?")
    }

    val grid = MutableList(largest { this.y } + 1) { MutableList(largest { this.x } + 1) { Material.Air } }

    // Place stone in grid:
    input.forEach { shape ->
        (1 until shape.size).forEach { i ->
            val curr = shape[i]
            val prev = shape[i - 1]

            when {
                curr.x == prev.x -> (min(curr.y, prev.y)..max(curr.y, prev.y)).forEach {
                    grid[it][curr.x] = Material.Stone
                }

                curr.y == prev.y -> (min(curr.x, prev.x)..max(curr.x, prev.x)).forEach {
                    grid[curr.y][it] = Material.Stone
                }

                else -> throw Exception("Didn't expect diagonal lines!")
            }
        }
    }

    return grid
}

fun Grid.nextSandPosition(sand: Coordinates): Coordinates? =
    when {
        // Check down:
        sand.y + 1 > this.size - 1 -> null
        this[sand.y + 1][sand.x] == Material.Air -> Coordinates(sand.x, sand.y + 1)

        // Check down-left:
        sand.x - 1 < 0 -> null
        this[sand.y + 1][sand.x - 1] == Material.Air -> Coordinates(sand.x - 1, sand.y + 1)

        // Check down-right:
        sand.x + 1 > this.first().size - 1 -> null
        this[sand.y + 1][sand.x + 1] == Material.Air -> Coordinates(sand.x + 1, sand.y + 1)

        // Otherwise, sand remains as is:
        else -> sand
    }

private typealias Blocked = Boolean

fun Grid.dropSand(
    source: Coordinates,
    stopWhenSourceBlocked: Boolean = false,
    stopWhenOutOfBounds: Boolean = false
): Blocked {
    if (this[source.y][source.x] != Material.Air) {
        if (stopWhenSourceBlocked) return false else throw Exception("Source of sand is blocked!")
    }

    var position = Coordinates(source.x, source.y)

    while (true) {
        when (val nextPosition = this.nextSandPosition(position)) {
            null -> {
                if (stopWhenOutOfBounds) return false else throw Exception("Sand is out of bounds!")
            }

            position -> {
                this[position.y][position.x] = Material.Sand
                return true
            }

            else -> position = nextPosition
        }
    }
}

val source = Coordinates(500, 0)

// Part 1:
buildGrid().let {
    var sandAtRest = 0
    while (it.dropSand(source, stopWhenOutOfBounds = true)) sandAtRest += 1
    println(sandAtRest)
}

// Part 2:
buildGrid().let {
    // Expand the grid:
    val infiniteRowsBelow = listOf(Material.Air, Material.Stone)
    val sourceHeight = (it.size - 1) + infiniteRowsBelow.size

    val missingLeft = max(0, 0 - (source.x - sourceHeight))
    val missingRight = max(0, (source.x + sourceHeight) - (it.first().size - 1))

    it.forEach { row ->
        row.addAll(0, List(missingLeft) { Material.Air })
        row.addAll(List(missingRight) { Material.Air })
    }
    val shiftedSource = Coordinates(source.x + missingLeft, source.y)

    infiniteRowsBelow.forEach { material -> it.add(MutableList(it.first().size) { material }) }

    // Note: Since sand falls straight down or diagonally at 45 degrees, then the farthest left or right it can reach is
    // equal to the farthest vertical height it can travel. So figure out the height of the source from the infinite
    // floor, then use that to figure out how many tiles should be tacked onto the left and right of the grid.

    var sandAtRest = 0
    while (it.dropSand(shiftedSource, stopWhenSourceBlocked = true)) sandAtRest += 1
    println(sandAtRest)
}
