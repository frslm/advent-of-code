import java.io.File
import kotlin.math.abs

val lines = File("input.txt").readLines()

// Part 1:
println(
    lines
        .map { it.split("   ") }
        .map { it.first().toInt() to it.last().toInt() }
        .unzip()
        .let { it.first.sorted().zip(it.second.sorted()) }
        .sumOf { abs(it.first - it.second) }
)

// Part 2:
println(
    lines
        .map { it.split("   ") }
        .map { it.first().toInt() to it.last().toInt() }
        .unzip()
        .let { (first, second) ->
            second.groupingBy { it }.eachCount().let { frequencyMap ->
                first.sumOf { it * (frequencyMap[it] ?: 0) }
            }
        }
)
