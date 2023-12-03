import java.io.File
import java.lang.Integer.max
import java.util.Objects.hash
import kotlin.math.abs

class Coordinates(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean = other is Coordinates && x == other.x && y == other.y
    override fun hashCode(): Int = hash(x, y)
}

class Report(val sensor: Coordinates, val beacon: Coordinates)

fun manhattanDistance(a: Coordinates, b: Coordinates) = abs(a.x - b.x) + abs(a.y - b.y)

class Range(val left: Int, val right: Int) {
    init {
        if (left > right) throw Exception("Left edge must never come after the right edge!")
    }
}

fun Iterable<Range>.deduplicate(): List<Range> {
    // Sort by the left edge first:
    val ranges = this.sortedWith { left, right ->
        when {
            left.left < right.left -> -1
            left.left > right.left -> 1
            else -> 0
        }
    }.toMutableList()

    // Merge any overlapping ranges:
    var i = 0
    while (i < ranges.size - 1) {
        val curr = ranges[i]
        val next = ranges[i + 1]

        if (next.left <= curr.right) {
            ranges[i] = Range(curr.left, max(curr.right, next.right))
            ranges.removeAt(i + 1)
        } else {
            i += 1
        }
    }

    return ranges.toList()
}

fun Iterable<Range>.contains(int: Int): Boolean {
    this.forEach { if (it.left <= int && int <= it.right) return true }
    return false
}

fun parseReports(): List<Report> {
    val match: String.() -> MatchGroupCollection = {
        Regex("Sensor at x=([-\\d]+), y=([-\\d]+): closest beacon is at x=([-\\d]+), y=([-\\d]+)")
            .matchEntire(this)?.groups
            ?: throw Exception("Unexpected format!")
    }

    val int: MatchGroupCollection.(Int) -> Int = { get(it)?.value?.toInt() ?: throw Exception("Unexpected format!") }

    return File("input.txt").readLines().map { line ->
        line.match()
            .let { Report(sensor = Coordinates(it.int(1), it.int(2)), beacon = Coordinates(it.int(3), it.int(4))) }
    }
}

enum class Comparison { GreaterThan, LessThan }

fun Int.within(other: Int, comparison: Comparison) =
    when (comparison) {
        Comparison.LessThan -> this <= other
        Comparison.GreaterThan -> this >= other
    }

class Diagonal(val a: Coordinates, val b: Coordinates) {
    init {
        if (abs(a.y - b.y) == abs(a.x - b.x)) throw Exception("Diagonal must be at a 45 degree slant!")
    }

    private fun intersectVertical(x: Int): Coordinates? = when {
        a.x <= x && x <= b.x -> Coordinates(x, a.y + (if (a.y <= b.y) +1 else -1) * abs(x - a.x))
        b.x <= x && x <= a.x -> Coordinates(x, b.y + (if (b.y <= a.y) +1 else -1) * abs(x - b.x))
        else -> null
    }

    private fun intersectHorizontal(y: Int): Coordinates? = when {
        a.y <= y && y <= b.y -> Coordinates(a.x + (if (a.x <= b.x) +1 else -1) * abs(y - a.y), y)
        b.y <= y && y <= a.y -> Coordinates(b.x + (if (b.x <= a.x) +1 else -1) * abs(y - a.y), y)
        else -> null
    }

    fun clipVertical(bounds: Int, comparison: Comparison): Diagonal? =
        when {
            a.x.within(bounds, comparison) && b.x.within(bounds, comparison) -> this
            !a.x.within(bounds, comparison) && !b.x.within(bounds, comparison) -> null
            else -> Diagonal(intersectVertical(bounds)!!, if (a.x.within(bounds, comparison)) a else b)
        }

    fun clipHorizontal(bounds: Int, comparison: Comparison): Diagonal? =
        when {
            a.y.within(bounds, comparison) && b.y.within(bounds, comparison) -> this
            !a.y.within(bounds, comparison) && !b.y.within(bounds, comparison) -> null
            else -> Diagonal(intersectHorizontal(bounds)!!, if (a.y.within(bounds, comparison)) a else b)
        }
}

class Diamond(val center: Coordinates, val radius: Int)

fun Diagonal.subtract(diamond: Diamond): Diagonal? {
    val deltaA = manhattanDistance(this.a, diamond.center)
    val deltaB = manhattanDistance(this.b, diamond.center)

    return when {
        deltaA <= diamond.radius && deltaB <= diamond.radius -> null
        deltaA > diamond.radius && deltaB > diamond.radius -> {
            null
        } // TODO: Should handle this somehow; both endpoints outside the diamond, _BUT_ the connecting line could still pass through!
        else -> {
            null
        } // TODO: Should handle this somehow.
    }
}

val reports = parseReports()

// Part 1:
val row = 2000000

val beaconlessRanges = reports.mapNotNull {
    val radius = manhattanDistance(it.sensor, it.beacon) - abs(it.sensor.y - row)
    if (radius >= 0) Range(it.sensor.x - radius, it.sensor.x + radius) else null
}.deduplicate()

println(
    beaconlessRanges.sumOf { it.right - it.left + 1 } - reports.map { it.beacon }.filter { it.y == row }.toSet()
        .filter { beaconlessRanges.contains(it.x) }.size
)

// Part 2:

// This one's tricky. One approach is to check each sensor's perimeter against all other sensor radii, and cut out
// intersecting chunks of the perimeter. If a sensor is left with a non-intersected chunk, then that must contain
// the missing beacon.

val minBounds = 0
val maxBounds = 4000000

reports.forEach { report ->
    val radius = manhattanDistance(report.sensor, report.beacon)

    var perimeter = listOf(
        Diagonal( // (top-right)
            Coordinates(report.sensor.x + 1, report.sensor.y - radius),
            Coordinates(report.sensor.x + radius + 1, report.sensor.y)
        ),
        Diagonal( // (bottom-right)
            Coordinates(report.sensor.x + radius, report.sensor.y + 1),
            Coordinates(report.sensor.x, report.sensor.y + radius + 1)
        ),
        Diagonal( // (bottom-left)
            Coordinates(report.sensor.x - 1, report.sensor.y + radius),
            Coordinates(report.sensor.x - radius - 1, report.sensor.y)
        ),
        Diagonal( // (top-left)
            Coordinates(report.sensor.x - radius, report.sensor.y - 1),
            Coordinates(report.sensor.x, report.sensor.y - radius - 1)
        )
    )

        // Clip bounds:
        .mapNotNull {
            it
                .clipVertical(minBounds, Comparison.GreaterThan)
                ?.clipVertical(maxBounds, Comparison.LessThan)
                ?.clipHorizontal(minBounds, Comparison.GreaterThan)
                ?.clipHorizontal(maxBounds, Comparison.LessThan)
        }

    reports.forEach {
        val diamond = Diamond(it.sensor, manhattanDistance(it.sensor, it.beacon))
        perimeter = perimeter.mapNotNull { diagonal -> diagonal.subtract(diamond) }
    }

    if (perimeter.isNotEmpty()) {
        if (perimeter.size == 1 && perimeter.first().a == perimeter.first().b) {
            perimeter.first().a.let {
                println(it.x * 4000000 + it.y)
                return@forEach
            }
        } else {
            throw Exception("Have more than one un-scanned tile?")
        }
    }
}
