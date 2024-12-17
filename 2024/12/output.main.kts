import java.io.File

val lines = File("input.txt").readLines()

typealias Point = Pair<Int, Int>
typealias Vector = Pair<Int, Int>

fun <T> Point.within(grid: List<List<T>>): Boolean = first >= 0 && first < grid.size && second >= 0 && second < grid.first().size
operator fun Point.plus(vector: Vector) = first + vector.first to second + vector.second
operator fun <T> List<List<T>>.get(point: Point): T = this[point.first][point.second]

enum class Direction { U, D, L, R }

val Point.neighbours get(): List<Pair<Point, Direction>> =
    listOf(
        (-1 to 0) to Direction.U,
        (1 to 0) to Direction.D,
        (0 to -1) to Direction.L,
        (0 to 1) to Direction.R
    ).map { (heading, direction) ->
        this + heading to direction
    }

fun <T, R> List<List<T>>.mapEachElement(operate: (Point) -> R): List<R> =
    this.flatMapIndexed { row, line ->
        line.mapIndexed { col, _ ->
            operate(row to col)
        }
    }

typealias Region = Int

data class Plot(
    val plant: Char,
    var region: Region? = null
)

fun List<List<Plot>>.assignGroupFrom(point: Point, region: Region) {
    if (this[point].region == null) {
        this[point].region = region

        point.neighbours.forEach { (neighbour, _) ->
            if (neighbour.within(this) && this[neighbour].plant == this[point].plant) {
                this.assignGroupFrom(neighbour, region)
            }
        }
    }
}

val regionGenerator: Iterator<Region> = sequence { var region = 0; while (true) { yield(region); region++ } }.iterator()

val farm = lines
    .map { line -> line.map { plant -> Plot(plant) } }
    .apply { this.mapEachElement { this.assignGroupFrom(it, regionGenerator.next()) } }

// Part 1:
data class Measurements(
    var area: Int = 0,
    var perimeter: Int = 0
)

println(
    mutableMapOf<Region, Measurements>().apply {
        farm.mapEachElement { point ->
            farm[point].region?.let { region ->
                this.getOrPut(region) { Measurements() }.apply {
                    area += 1
                    perimeter += 4 - point.neighbours.count { (neighbour, _) ->
                        neighbour.within(farm) && farm[neighbour].region == farm[point].region
                    }
                }
            }
        }
    }.map { (_, measurements) ->
        measurements.area * measurements.perimeter
    }.sum()
)

// Part 2:
fun <A, B> List<Pair<A, B>>.groupByFirst(): Map<A, List<B>> =
    this.groupBy { it.first }.mapValues { (_, pairs) -> pairs.map { it.second } }

fun <A, B> List<Pair<A, B>>.groupBySecond(): Map<B, List<A>> =
    this.groupBy { it.second }.mapValues { (_, pairs) -> pairs.map { it.first } }

println(
    // For each region...
    farm.mapEachElement { farm[it].region }.filterNotNull().toSet().sumOf { region ->

        // ...get all of its individual edges...
        farm.mapEachElement { point ->
            if (farm[point].region == region) {
                point.neighbours.filter { (neighbour, _) ->
                    !neighbour.within(farm) || farm[neighbour].region != region
                }
            } else {
                emptyList()
            }
        }.flatten()

            // ...for each collection of edges grouped by direction...
            .groupBySecond().flatMap { (direction, points) ->
                when (direction) {

                    // ...group contiguous edges in the same row together to form a side...
                    Direction.U, Direction.D -> mutableListOf<MutableList<Point>>().apply {
                        points.groupByFirst().mapValues { (_, cols) -> cols.sorted() }.forEach { (row, cols) ->
                            cols.forEachIndexed { index, col ->
                                if (index == 0 || cols[index - 1] != cols[index] - 1) add(mutableListOf())
                                last().add(row to col)
                            }
                        }
                    }

                    // ...group contiguous edges in the same column together to form a side...
                    Direction.L, Direction.R -> mutableListOf<MutableList<Point>>().apply {
                        points.groupBySecond().mapValues { (_, rows) -> rows.sorted() }.forEach { (col, rows) ->
                            rows.forEachIndexed { index, row ->
                                if (index == 0 || rows[index - 1] != rows[index] - 1) add(mutableListOf())
                                last().add(row to col)
                            }
                        }
                    }
                }
            }

            // ...then multiply the area by the number of sides:
            .let { sides ->
                farm.mapEachElement { farm[it].region == region }.count { it } * sides.count()
            }
    }
)
