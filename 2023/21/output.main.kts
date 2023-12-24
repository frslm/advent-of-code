import java.io.File

typealias Map = List<List<Char>>

typealias Row = Int; typealias Col = Int
data class Location(val row: Row, val col: Col)
fun List<String>.getMapAndStartAt(start: Char): Pair<Map, Location> {
    val starts = mutableSetOf<Location>()

    return this.mapIndexed { row, tiles ->
        tiles.mapIndexed { col, tile ->
            when (tile) {
                start -> '.'.also { starts.add(Location(row, col)) }
                else -> tile
            }
        }
    } to starts.single()
}

operator fun Map.get(location: Location) = this[location.row][location.col]

fun Map.traverseOneStepFrom(locations: Set<Location>): Set<Location> {
    val destinations = mutableSetOf<Location>()

    locations.forEach { location ->
        destinations += listOfNotNull(
            location.copy(row = location.row - 1).let { if (it.row >= 0) it else null },
            location.copy(row = location.row + 1).let { if (it.row < this.size) it else null },
            location.copy(col = location.col - 1).let { if (it.col >= 0) it else null },
            location.copy(col = location.col + 1).let { if (it.col < this.first().size) it else null }
        ).filter {
            this[it] == '.'
        }
    }

    return destinations
}

val (map, start) = File("input.txt").readLines().getMapAndStartAt('S')

// Part 1:

// Seems like breadth-first flood fill is the way to go; just stop the simulation after the required number of steps
// are taken.

var locations = setOf(start)
repeat(64) { locations = map.traverseOneStepFrom(locations) }
println(locations.size)

// Part 2:
println(

)
