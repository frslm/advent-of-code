import java.io.File

enum class Direction {
    N, S, W, E;
    val opposite get() = when (this) { N -> S; S -> N; W -> E; E -> W }
}

data class Pipe(
    val entrance: Direction,
    val exit: Direction,
    val left: Set<Direction>,
    val right: Set<Direction>
) {
    fun flip() = Pipe(entrance = exit, exit = entrance, left = right, right = left)
}

val pipes = mapOf(
    '|' to Pipe(entrance = Direction.N, exit = Direction.S, left = setOf(Direction.E), right = setOf(Direction.W)),
    '-' to Pipe(entrance = Direction.W, exit = Direction.E, left = setOf(Direction.N), right = setOf(Direction.S)),
    'F' to Pipe(entrance = Direction.S, exit = Direction.E, left = setOf(Direction.N, Direction.W), right = emptySet()),
    '7' to Pipe(entrance = Direction.S, exit = Direction.W, left = emptySet(), right = setOf(Direction.N, Direction.E)),
    'J' to Pipe(entrance = Direction.N, exit = Direction.W, left = setOf(Direction.S, Direction.E), right = emptySet()),
    'L' to Pipe(entrance = Direction.N, exit = Direction.E, left = setOf(), right = setOf(Direction.S, Direction.W)),
)

fun Char.getPipeWhenEnteringThrough(direction: Direction): Pipe? =
    pipes[this]?.let { when (direction.opposite) { it.entrance -> it; it.exit -> it.flip(); else -> null } }
    // Note: Flip to the opposite direction since entering a pipe uses the heading _into_ the pipe, whereas each pipe
    //       defines its connections going _out of_ its center.

typealias Row = Int; typealias Col = Int
data class Location(var row: Row, var col: Col)
data class Pose(val location: Location, var heading: Direction)

operator fun <T> List<List<T>>.get(location: Location) = this[location.row][location.col]
operator fun <T> List<MutableList<T>>.set(location: Location, value: T) { this[location.row][location.col] = value }

fun <T> List<List<T>>.getNeighbours(location: Location): Map<Direction, Location?> =
    listOf(
        Direction.N to if (location.row - 1 >= 0) Location(location.row - 1, location.col) else null,
        Direction.S to if (location.row + 1 < this.size) Location(location.row + 1, location.col) else null,
        Direction.W to if (location.col - 1 >= 0) Location(location.row, location.col - 1) else null,
        Direction.E to if (location.col + 1 < this.first().size) Location(location.row, location.col + 1) else null
    ).toMap()

fun <T> List<List<T>>.traverseThroughPipeFrom(start: Pose, getChar: T.() -> Char, operate: T.(Location, Pipe) -> Unit) {
    val pose = start.copy(location = start.location.copy())
    while (true) {
        when (pose.heading) {
            Direction.N -> pose.location.row--; Direction.W -> pose.location.col--
            Direction.S -> pose.location.row++; Direction.E -> pose.location.col++
        }

        this[pose.location].let { element ->
            element.getChar().getPipeWhenEnteringThrough(pose.heading)!!.let { pipe ->
                pipe.let { pose.heading = it.exit }
                element.operate(pose.location, pipe)
            }
        }

        if (pose.location == start.location) break
    }
}

fun <T> List<List<T>>.traverseEach(operate: T.(Location) -> Unit) {
    this.forEachIndexed { row: Row, elements ->
        elements.forEachIndexed { col: Col, element ->
            element.operate(Location(row, col))
        }
    }
}

fun List<String>.getGridAndStartAt(start: Char): Pair<List<MutableList<Char>>, Pose> {
    val grid = this.map { it.toMutableList() }

    val location = mutableListOf<Location>().also { map ->
        grid.traverseEach { if (this == start) map.add(it) }
    }.first()

    // Get the connecting entrance and exit (which one's which is arbitrarily chosen):
    val (entrance, exit) = grid.getNeighbours(location).mapNotNull { (direction, location) ->
        location?.let { grid[it] }?.getPipeWhenEnteringThrough(direction)?.let { direction }
    }

    // Replace the start char with the appropriate pipe char:
    grid[location] = pipes.mapNotNull { (char, pipe) ->
        if (
            (pipe.entrance == entrance && pipe.exit == exit) ||
            (pipe.entrance == exit && pipe.exit == entrance)
        ) char else null
    }.first()

    return grid to Pose(location, entrance)
}

val (grid, start) = File("input.txt").readLines().getGridAndStartAt('S')

// Part 1:
var steps = 0
grid.traverseThroughPipeFrom(start, { this }) { _, _ -> steps++ }
println(steps / 2) // (the step count at the opposite end of the pipe loop occurs halfway through the traversal)

// Part 2:

// Can first start by marking on the map everything that's part of the main pipe loop. Then for all the remaining
// unmarked parts of the map, flood fill all the different areas, marking each with its own ID. For any area that
// touches the map edge, mark it as being outside. Traverse the main loop and mark adjacent tiles as being to the left
// or right of the main loop path. If either the left or right side touches the map edge or an outside area, then define
// either left or right as being outside. Once done traversing, mark all known areas as inside (if left is outside, then
// all right areas are inside, or vice versa), then finally count the total number of tiles marked inside.

fun <T> List<List<T>>.traverseFloodFrom(
    start: Location,
    isConnected: T.() -> Boolean,
    onEdgeHit: T.() -> Unit,
    operate: T.() -> Unit
) {
    this[start].let { element ->
        if (element.isConnected()) {
            element.operate()

            this.getNeighbours(start).values.forEach { location ->
                location?.let { this.traverseFloodFrom(it, isConnected, onEdgeHit, operate) } ?: element.onEdgeHit()
            }
        }
    }
}

// Enhance the grid with area info:
typealias AreaId = Long
data class Tile(val char: Char, var areaId: AreaId? = null)
val gridWithAreas = grid.map { row -> row.map { Tile(it) } }

// Give the main pipe loop its own area ID:
val idGenerator = sequence { var id = 0L; while(true) { yield(id++) } }.iterator()
val mainLoopAreaId = idGenerator.next()
gridWithAreas.traverseThroughPipeFrom(start, { char }) { _, _ -> areaId = mainLoopAreaId }

// Give each separate area its own area ID, and (if possible) track whether it's outside:
typealias Outside = Boolean?
val areasOutside = mutableSetOf<AreaId>()
gridWithAreas.traverseEach { location ->
    if (areaId == null) {
        idGenerator.next().let { id ->
            gridWithAreas.traverseFloodFrom(
                location,
                isConnected = { areaId == null },
                onEdgeHit = { areasOutside.add(id) },
                operate = { areaId = id }
            )
        }
    }
}

// Mark any areas to the left and right of the main pipe loop, and (if possible) track whether they're outside:
data class Areas(var outside: Outside = null, val ids: MutableSet<AreaId> = mutableSetOf())
val leftOfMainLoop = Areas(); val rightOfMainLoop = Areas()
gridWithAreas.traverseThroughPipeFrom(start, { char }) { location, pipe ->
    val neighbours = gridWithAreas.getNeighbours(location)

    listOf(
        pipe.left to leftOfMainLoop,
        pipe.right to rightOfMainLoop
    ).forEach { (directions, areas) ->
        directions.forEach { direction ->
            neighbours[direction]?.let { location ->
                gridWithAreas[location].areaId!!.let { id ->
                    if (id != mainLoopAreaId) areas.ids.add(id)
                }
            } ?: run { areas.outside = true }
        }
    }
}

// Consolidate both "is outside" checks:
if (leftOfMainLoop.ids.intersect(areasOutside).isNotEmpty()) { leftOfMainLoop.outside = true }
if (rightOfMainLoop.ids.intersect(areasOutside).isNotEmpty()) { rightOfMainLoop.outside = true }
// Note: This is needed to completely handle the two extreme scenarios:
//           1. The main loop completely covers the entire edge of the map.
//              ^ The "is outside" check from the main loop is required.
//           2. The main loop doesn't touch any map edge.
//              ^ The "is outside" check from the other areas is required.
//       All other scenarios lie between the above two, so they're fine with only one set of "is outside" checks.

// Count all tiles that are on the side of the main pipe loop that is definitely not outside:
val areasInsideLoop = when {
    leftOfMainLoop.outside == true -> rightOfMainLoop.ids
    rightOfMainLoop.outside == true -> leftOfMainLoop.ids
    else -> null!!
}

var tilesWithinLoop = 0L
gridWithAreas.traverseEach { if (this.areaId in areasInsideLoop) tilesWithinLoop++ }
println(tilesWithinLoop)
