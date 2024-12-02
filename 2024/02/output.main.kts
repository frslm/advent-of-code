import java.io.File
import kotlin.math.abs

val lines = File("input.txt").readLines()

private fun List<Int>.safe(): Boolean =
    (1 until this.size).map { index -> this[index] - this[index - 1] }.let { diffs ->
        (diffs.all { it > 0 } || diffs.all { it < 0 }) && diffs.all { abs(it) in (1..3) }
    }

// Part 1:
println(
    lines
        .asSequence()
        .map { line -> line.split(' ').map { it.toInt() } }
        .map { it.safe() }
        .map { if (it) 1 else 0 }
        .sum()
)

// Part 2:
println(
    lines
        .asSequence()
        .map { line -> line.split(' ').map { it.toInt() } }
        .map { it.safe() || it.indices.any { index -> it.filterIndexed { i, _ -> i != index }.safe() } }
        .map { if (it) 1 else 0 }
        .sum()
)
