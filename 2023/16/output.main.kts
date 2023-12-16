import java.io.File

typealias Row = Int; typealias Col = Int
data class Location(var row: Row, var col: Col)
enum class Direction { N, S, W, E }
data class Pose(val location: Location, var heading: Direction) {
    fun moveOneStep() {
        when (heading) {
            Direction.N -> location.row--; Direction.W -> location.col--
            Direction.S -> location.row++; Direction.E -> location.col++
        }
    }

    fun copyWith(heading: Direction? = null) = copy(location = location.copy(), heading = heading ?: this.heading)
}

data class Tile(val contents: Char) { var energized: Boolean = false }
typealias Contraption = List<List<Tile>>

fun Contraption.propagateBeamFrom(pose: Pose, alreadySeen: MutableSet<Pose> = mutableSetOf()) {
    while (true) {
        pose.moveOneStep()
        if (
            pose in alreadySeen ||
            pose.location.row < 0 || pose.location.row >= this.size ||
            pose.location.col < 0 || pose.location.col >= this.first().size
        ) break

        alreadySeen.add(pose.copy(location = pose.location.copy()))

        when (this[pose.location.row][pose.location.col].also { it.energized = true }.contents) {
            '.' -> {} // (maintain direction)
            '/' -> pose.heading = when (pose.heading) {
                Direction.N -> Direction.E
                Direction.S -> Direction.W
                Direction.W -> Direction.S
                Direction.E -> Direction.N
            }
            '\\' -> pose.heading = when (pose.heading) {
                Direction.N -> Direction.W
                Direction.S -> Direction.E
                Direction.W -> Direction.N
                Direction.E -> Direction.S
            }
            '|' -> when (pose.heading) {
                Direction.N, Direction.S -> {} // (maintain direction)
                Direction.W, Direction.E -> {
                    pose.heading = Direction.N
                    this.propagateBeamFrom(pose.copy(location = pose.location.copy(), heading = Direction.S), alreadySeen)
                }
            }
            '-' -> when (pose.heading) {
                Direction.W, Direction.E -> {} // (maintain direction)
                Direction.N, Direction.S -> {
                    pose.heading = Direction.W
                    this.propagateBeamFrom(pose.copy(location = pose.location.copy(), heading = Direction.E), alreadySeen)
                }
            }
        }
    }
}

val contraption: Contraption = File("input.txt").readLines().map { line -> line.map { Tile(it) } }

fun Contraption.countEnergizedTilesAfterPropagatingBeamFrom(pose: Pose) =
    this
        .onEach { it.forEach { tile -> tile.energized = false } }
        .also { it.propagateBeamFrom(pose) }
        .sumOf { it.count { tile -> tile.energized } }

// Part 1:

// Watch out for loops that trap the beam; exit early if a beam propagates through an already-seen pose.

println(
    contraption.countEnergizedTilesAfterPropagatingBeamFrom(Pose(Location(row = 0, col = -1), Direction.E))
)

// Part 2:

// Just repeat the simulation from each edge; performance is not a problem.

println(
    listOf(
        (0 until contraption.first().size).maxOf { col ->
            contraption.countEnergizedTilesAfterPropagatingBeamFrom(Pose(Location(row = -1, col), Direction.S))
        },
        (0 until contraption.first().size).maxOf { col ->
            contraption.countEnergizedTilesAfterPropagatingBeamFrom(Pose(Location(row = contraption.size, col), Direction.N))
        },
        contraption.indices.maxOf { row ->
            contraption.countEnergizedTilesAfterPropagatingBeamFrom(Pose(Location(row, col = -1), Direction.E))
        },
        contraption.indices.maxOf { row ->
            contraption.countEnergizedTilesAfterPropagatingBeamFrom(Pose(Location(row, col = contraption.first().size), Direction.W))
        }
    ).max()
)
