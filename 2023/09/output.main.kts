import java.io.File

val histories = File("input.txt").readLines().map { line ->
    line.split(" ").map { it.toLong() }
}

fun List<Long>.getSequences(): List<List<Long>> =
    mutableListOf(this).also { sequences ->
        while (!sequences.last().all { it == 0L }) {
            sequences.add(sequences.last().zipWithNext { left, right -> right - left })
        }
    }

// Part 1:
println(
    histories.sumOf { history ->
        history
            .getSequences().reversed()
            .map { it.last() }.reduce { next, value -> next + value }
    }
)

// Part 2:
println(
    histories.sumOf { history ->
        history
            .getSequences().reversed()
            .map { it.first() }.reduce { previous, value -> value - previous }
    }
)
