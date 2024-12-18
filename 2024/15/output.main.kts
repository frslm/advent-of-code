import java.io.File
import kotlin.math.min

typealias Vector = Pair<Int, Int>
operator fun Vector.plus(vector: Vector): Vector = first + vector.first to second + vector.second
operator fun Vector.minus(vector: Vector): Vector = first - vector.first to second - vector.second
operator fun <T> List<List<T>>.get(vector: Vector): T = this[vector.first][vector.second]
operator fun <T> List<MutableList<T>>.set(vector: Vector, element: T) { this[vector.first][vector.second] = element }

enum class Direction { U, D, L, R }

val movements = File("input.txt").readLines().let { lines ->
    lines.subList(lines.indexOf("") + 1, lines.size)
        .joinToString("")
        .mapNotNull { char ->
            when (char) {
                '^' -> Direction.U
                'v' -> Direction.D
                '<' -> Direction.L
                '>' -> Direction.R
                else -> null
            }
        }
}

// Part 1:
enum class Entity { Wall, Box, Robot }

val warehouse = File("input.txt").readLines().let { lines ->
    lines.subList(0, lines.indexOf(""))
        .map { line ->
            line.map { char ->
                when (char) {
                    '#' -> Entity.Wall
                    'O' -> Entity.Box
                    '@' -> Entity.Robot
                    else -> null
                }
            }.toMutableList()
        }
}

fun List<MutableList<Entity?>>.moveFrom(location: Vector, direction: Direction): Vector =
    when (direction) {
        Direction.U -> (-1 to 0)
        Direction.D -> (1 to 0)
        Direction.L -> (0 to -1)
        Direction.R -> (0 to 1)
    }.let { heading ->
        val neighbour = location + heading

        when (this[neighbour]) {
            Entity.Robot -> throw Exception("Should never run into the robot!")
            Entity.Wall -> location
            Entity.Box ->
                if (this.moveFrom(neighbour, direction) == neighbour) {
                    location
                } else {
                    neighbour.also {
                        this[it] = this[location]
                        this[location] = null
                    }
                }
            null -> neighbour.also {
                this[it] = this[location]
                this[location] = null
            }
        }
    }

var robot: Vector = warehouse
    .map { it.indexOf(Entity.Robot) }
    .let { cols -> cols.indexOfFirst { it != -1 }.let { it to cols[it] } }

movements.forEach { direction -> robot = warehouse.moveFrom(robot, direction) }

println(
    warehouse.flatMapIndexed { row, line ->
        line.mapIndexedNotNull { col, entity ->
            if (entity == Entity.Box) {
                100 * row + col
            } else {
                null
            }
        }
    }.sum()
)

// Part 2:
enum class BigEntity { Wall, BoxLeft, BoxRight, Robot }

val bigWarehouse = File("input.txt").readLines().let { lines ->
    lines.subList(0, lines.indexOf(""))
        .map { line ->
            line.mapNotNull { char ->
                when (char) {
                    '#' -> "##"
                    'O' -> "[]"
                    '.' -> ".."
                    '@' -> "@."
                    else -> null
                }
            }.joinToString("")
        }
        .map { line ->
            line.map { char ->
                when (char) {
                    '#' -> BigEntity.Wall
                    '[' -> BigEntity.BoxLeft
                    ']' -> BigEntity.BoxRight
                    '@' -> BigEntity.Robot
                    else -> null
                }
            }.toMutableList()
        }
}

fun List<MutableList<BigEntity?>>.canMoveInto(destination: Vector, direction: Direction): Boolean =
    when (this[destination]) {
        BigEntity.Robot -> throw Exception("Should never run into the robot!")
        BigEntity.Wall -> false
        BigEntity.BoxLeft, BigEntity.BoxRight -> this.canMoveFrom(destination, direction)
        null -> true
    }

fun List<MutableList<BigEntity?>>.canMoveFrom(location: Vector, direction: Direction): Boolean =
    when (direction) {
        Direction.U -> (-1 to 0)
        Direction.D -> (1 to 0)
        Direction.L -> (0 to -1)
        Direction.R -> (0 to 1)
    }.let { heading ->
        when (direction) {
            Direction.L, Direction.R -> true
            Direction.U, Direction.D -> when (this[location]) {
                BigEntity.BoxLeft -> canMoveInto(location + heading + (0 to 1), direction)
                BigEntity.BoxRight -> canMoveInto(location + heading + (0 to -1), direction)
                else -> true
            }
        } && canMoveInto(location + heading, direction)
    }

fun List<MutableList<BigEntity?>>.moveInto(destination: Vector, direction: Direction): Vector =
    when (direction) {
        Direction.U -> (-1 to 0)
        Direction.D -> (1 to 0)
        Direction.L -> (0 to -1)
        Direction.R -> (0 to 1)
    }.let { heading ->
        when (this[destination]) {
            BigEntity.BoxLeft, BigEntity.BoxRight -> this.moveFrom(destination, direction)
            else -> {} // (no extra move to do)
        }

        destination.also {
            this[it] = this[destination - heading]
            this[destination - heading] = null
        }
    }

/** Can only be called if [canMoveFrom] returns `true` for the same arguments. */
@JvmName("moveFrom_Big")
fun List<MutableList<BigEntity?>>.moveFrom(location: Vector, direction: Direction): Vector =
    when (direction) {
        Direction.U -> (-1 to 0)
        Direction.D -> (1 to 0)
        Direction.L -> (0 to -1)
        Direction.R -> (0 to 1)
    }.let { heading ->
        when (direction) {
            Direction.L, Direction.R -> {} // (no extra move to do)
            Direction.U, Direction.D -> when (this[location]) {
                BigEntity.BoxLeft -> this.moveInto(location + heading + (0 to 1), direction)
                BigEntity.BoxRight -> this.moveInto(location + heading + (0 to -1), direction)
                else -> {} // (no extra move to do)
            }
        }

        this.moveInto(location + heading, direction)
    }

robot = bigWarehouse
    .map { it.indexOf(BigEntity.Robot) }
    .let { cols -> cols.indexOfFirst { it != -1 }.let { it to cols[it] } }

movements.forEach { direction ->
    if (bigWarehouse.canMoveFrom(robot, direction)) {
        robot = bigWarehouse.moveFrom(robot, direction)
    }
}

println(
    bigWarehouse.flatMapIndexed { row, line ->
        line.mapIndexedNotNull { col, entity ->
            if (entity == BigEntity.BoxLeft) {
                100 * row + col
                // Note: Problem has an error in its description; closest edge would instead use the following:
                // `100 * min(row, bigWarehouse.size - 1 - row) + min(col, bigWarehouse.first().size - 1 - col - 1)`
            } else {
                null
            }
        }
    }.sum()
)
