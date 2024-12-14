import java.io.File
import kotlin.system.measureTimeMillis

val lines = File("input.txt").readLines()

typealias Point = Pair<Int, Int>
typealias Vector = Pair<Int, Int>

val Point.inGrid get(): Boolean = first >= 0 && first < lines.size && second >= 0 && second < lines.first().length
fun Vector.rotate(): Vector = second to -first // (discrete rotation matrix)

operator fun Point.plus(vector: Vector) = first + vector.first to second + vector.second
operator fun Point.minus(vector: Vector) = first - vector.first to second - vector.second

operator fun List<String>.get(point: Point): Char = this[point.first][point.second]

val startHeading: Vector = -1 to 0
val startLocation: Point = lines.map { it.indexOf('^') }.let { cols ->
    cols.indexOfFirst { it != -1 }.let { it to cols[it] }
}

// Part 1:
println(
    mutableSetOf<Point>().apply {
        var heading = startHeading
        var location = startLocation

        while (location.inGrid) {
            val next = location + heading
            if (next.inGrid && lines[next] == '#') {
                heading = heading.rotate()
            } else {
                add(location)
                location = next
            }
        }
    }.size
)

// Part 2:

// Simulate steps until reaching an already-visited obstacle's slipstream (vector field that'll move an entity towards
// the obstacle), having stored the heading too, then check if current heading can be rotated into slipstream's heading,
// then check if obstacle can be placed within grid.

println(
    mutableSetOf<Point>().apply {
        var heading = startHeading
        var location = startLocation

        val slipstreams = mutableSetOf<Pair<Point, Vector>>()

        while (location.inGrid) {
            val next = location + heading

            if (next.inGrid && lines[next] == '#') {
                var slipstreamLocation = location
                while (slipstreamLocation.inGrid && lines[slipstreamLocation] != '#') {
                    slipstreams.add(slipstreamLocation to heading)
                    slipstreamLocation -= heading
                }

                heading = heading.rotate()
            } else {
                if ((location to heading.rotate()) in slipstreams) { add(next) }
                location = next
            }
        }
    }.size
)

// Unfortunately, the above doesn't seem to work - probably because it's possible to place a single obstacle that
// creates a completely new route that ends in a loop. Will just brute-force this instead.

println(
    lines.map { it.toMutableList() }.let { mutableLines ->
        mutableLines.flatMapIndexed { row, line ->
            line.mapIndexed { col, char ->
                var inLoop = false

                if (char != '#') {
                    mutableLines[row][col] = '#'

                    var heading = startHeading
                    var location = startLocation

                    val visited = mutableSetOf<Pair<Point, Vector>>()

                    measureTimeMillis {
                        while (location.inGrid) {
                            val next = location + heading
                            if (next.inGrid && mutableLines[next.first][next.second] == '#') {
                                heading = heading.rotate()
                            } else {
                                if (location to heading in visited) {
                                    inLoop = true
                                    break
                                } else {
                                    visited.add(location to heading)
                                    location = next
                                }
                            }
                        }
                    }

                    inLoop.also {
                        mutableLines[row][col] = char
                    }
                } else {
                    inLoop
                }
            }
        }.count { it }
    }
)
