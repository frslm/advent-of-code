import java.io.File

enum class Direction { U, D, L, R }

data class Action(
    val direction: Direction,
    val distance: Int,
    val code: String
)

val plan = File("input.txt").readLines().map { line ->
    Regex("([UDLR]) (\\d+) \\(#([\\da-z]{6})\\)").matchEntire(line)!!.groups.let {
        Action(
            direction = Direction.valueOf(it[1]!!.value),
            distance = it[2]!!.value.toInt(),
            code = it[3]!!.value
        )
    }
}

// Part 1:

// Physically build a grid then fill the interior. First attempted to flood-fill, but the stack overflowed, so switched
// to a traversal that records if a tile is inside the perimeter.

enum class Tile { Ground, Trench, InteriorTrench }

typealias Row = Int; typealias Col = Int
data class Location(var row: Row, var col: Col)

operator fun <T> List<List<T>>.get(location: Location) = this[location.row][location.col]
operator fun <T> List<MutableList<T>>.set(location: Location, value: T) { this[location.row][location.col] = value }

fun List<Action>.digInto(grid: List<MutableList<Tile>>, location: Location) {
    this.forEach { (direction, distance, _) ->
        when (direction) {
            Direction.U -> { repeat(distance) { location.row -= 1; grid[location] = Tile.Trench } }
            Direction.D -> { repeat(distance) { location.row += 1; grid[location] = Tile.Trench } }
            Direction.L -> { repeat(distance) { location.col -= 1; grid[location] = Tile.Trench } }
            Direction.R -> { repeat(distance) { location.col += 1; grid[location] = Tile.Trench } }
        }
    }
}

data class Extents(var up: Int, var down: Int, var left: Int, var right: Int)

fun List<Action>.getExtents(): Extents {
    val extents = Extents(0, 0, 0, 0)
    val location = Location(0, 0)

    this.forEach { (direction, distance, _) ->
        when (direction) {
            Direction.U -> { location.row -= distance; if (location.row < extents.up) extents.up = location.row }
            Direction.D -> { location.row += distance; if (location.row > extents.down) extents.down = location.row }
            Direction.L -> { location.col -= distance; if (location.col < extents.left) extents.left = location.col }
            Direction.R -> { location.col += distance; if (location.col > extents.right) extents.right = location.col }
        }
    }

    return extents
}

fun List<MutableList<Tile>>.traverseInterior(operate: List<MutableList<Tile>>.(Row, Col) -> Unit) {
    this.forEachIndexed { row, tiles ->
        var inside = false
        var trenchAbove = false
        var trenchBelow = false

        tiles.forEachIndexed { col, tile ->
            if (tile == Tile.Trench) {
                if (row - 1 >= 0 && this[row - 1][col] == Tile.Trench) trenchAbove = !trenchAbove
                if (row + 1 < this.size && this[row + 1][col] == Tile.Trench) trenchBelow = !trenchBelow

                if (trenchAbove && trenchBelow) {
                    trenchAbove = false
                    trenchBelow = false
                    inside = !inside
                }
            }

            if (inside && tile == Tile.Ground) this.operate(row, col)
        }
    }
}

val extents = plan.getExtents()

println(
    List(extents.down - extents.up + 1) { MutableList(extents.right - extents.left + 1) { Tile.Ground } }
        .also {
            val start = Location(0 - extents.up, 0 - extents.left)
            it[start] = Tile.Trench
            plan.digInto(it, start)
        }
        .also {
            it.traverseInterior { row, col -> this[row][col] = Tile.InteriorTrench }
        }
        .sumOf { it.count { tile -> tile == Tile.Trench || tile == Tile.InteriorTrench } }
)

// Part 2:

// Since the numbers are too big to physically build on a grid, maybe each vertex of the trench polygon can be stored
// in some list? Then perhaps rectangular chunks can be iteratively cut away from the polygon until nothing remains,
// with the size of each rectangle added to a running sum.

println(

)
