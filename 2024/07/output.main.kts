import java.io.File

val lines = File("input.txt").readLines().map { line ->
    line.split(": ").let { (left, right) ->
        left.toLong() to right.split(' ').map { it.toLong() }
    }
}

fun List<Long>.possibleResultsUsing(vararg operations: (Long, Long) -> Long): List<Long> =
    if (this.size == 1) {
        listOf(this.first())
    } else {
        operations.flatMap {
            (listOf(it(this[0], this[1])) + this.takeLast(this.size - 2)).possibleResultsUsing(*operations)
        }
    }

fun List<Pair<Long, List<Long>>>.calculateCalibrationResultUsing(vararg operations: (Long, Long) -> Long) =
    this
        .filter { (testValue, numbers) -> testValue in numbers.possibleResultsUsing(*operations) }
        .sumOf { (testValue, _) -> testValue }

// Part 1:
println(
    lines.calculateCalibrationResultUsing(
        { a, b -> a + b },
        { a, b -> a * b }
    )
)

// Part 2:
println(
    lines.calculateCalibrationResultUsing(
        { a, b -> a + b },
        { a, b -> a * b },
        { a, b -> "$a$b".toLong() }
    )
)
