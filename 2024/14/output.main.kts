import java.io.File

typealias Vector = Pair<Int, Int>

data class Robot(
    val position: Vector,
    val velocity: Vector
)

val robots = File("input.txt").readLines().mapNotNull { line ->
    Regex("p=(-?\\d+),(-?\\d+) v=(-?\\d+),(-?\\d+)").matchEntire(line)?.let { match ->
        Robot(
            position = match.groupValues[2].toInt() to match.groupValues[1].toInt(),
            velocity = match.groupValues[4].toInt() to match.groupValues[3].toInt()
        )
    }
}

// Part 1:
fun Robot.move(extents: Vector): Robot = this.copy(
    position =
        Math.floorMod(this.position.first + this.velocity.first, extents.first) to
        Math.floorMod(this.position.second + this.velocity.second, extents.second)
)

enum class Quadrant { NW, NE, SW, SE }

val extents = 103 to 101

println(
    robots
        .map { (0 until 100).fold(it) { robot, _ -> robot.move(extents) } }
        .groupBy { robot ->
            when {
                robot.position.first < extents.first / 2 && robot.position.second < extents.second / 2 -> Quadrant.NW
                robot.position.first < extents.first / 2 && robot.position.second > extents.second / 2 -> Quadrant.NE
                robot.position.first > extents.first / 2 && robot.position.second < extents.second / 2 -> Quadrant.SW
                robot.position.first > extents.first / 2 && robot.position.second > extents.second / 2 -> Quadrant.SE
                else -> null
            }
        }
        .mapNotNull { (quadrant, robots) -> quadrant?.let { robots.size } }
        .reduce { acc, i -> acc * i }
)

// Part 2:

// Set up a grid printer that waits on user input (using `readln`), then just hold Enter to keep generating grids.
// Started noticing some rows or columns with many hits, so added a filter to only wait on grids that have many robots
// in the same row or column (representing edges). Further fine-tuned it to look for contiguous edges only.

val grid get() = MutableList(extents.first) { MutableList(extents.second) { ' ' } }

fun List<Robot>.print() {
    grid
        .also { this.forEach { robot -> it[robot.position.first][robot.position.second] = 'O' } }
        .map { it.joinToString("") }
        .forEach { println(it) }
}

var seconds = 0
var arrangement = robots

while(true) {
    if (
        mutableListOf<MutableList<Vector>>().apply {
            arrangement
                .groupBy { it.position.second }
                .mapValues { (_, robots) -> robots.map { it.position.first }.sorted() }
                .forEach { (col, rows) ->
                    rows.forEachIndexed { index, row ->
                        if (index == 0 || rows[index - 1] != rows[index] - 1) add(mutableListOf())
                        last().add(row to col)
                    }
                }
        }.any { it.size >= 10 } // (arbitrary size for length of edge to look for)
    ) {
        println("After $seconds seconds:")
        arrangement.print()
        readln()
    }

    arrangement = arrangement.map { it.move(extents) }
    seconds++
}
