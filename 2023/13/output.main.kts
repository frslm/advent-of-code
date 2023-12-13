import java.io.File

typealias Grid = List<String>

val grids: List<Grid> = mutableListOf(mutableListOf<String>()).also {
    File("input.txt").readLines().forEach { line ->
        if (line.isNotEmpty()) it.last().add(line) else it.add(mutableListOf())
    }
}
.map { it.toList() }.toList()

enum class Orientation { Vertical, Horizontal }

fun Grid.findMirror(expectedMismatches: Int): Pair<Orientation, Int> {
    // Look for vertical mirrors:
    (0 until this.first().length - 1).forEach { col ->
        var mismatches = 0
        var distance = 0

        while (0 <= col - distance && col + 1 + distance < this.first().length) {
            indices.forEach { row ->
                if (this[row][col - distance] != this[row][col + 1 + distance]) mismatches++
            }

            if (mismatches > expectedMismatches) break
            distance++
        }

        if (mismatches == expectedMismatches) return Orientation.Vertical to col + 1
    }

    // Look for horizontal mirrors:
    (0 until this.size - 1).forEach { row ->
        var mismatches = 0
        var distance = 0

        while (0 <= row - distance && row + 1 + distance < this.size) {
            (0 until this.first().length).forEach { col ->
                if (this[row - distance][col] != this[row + 1 + distance][col]) mismatches++
            }

            if (mismatches > expectedMismatches) break
            distance++
        }

        if (mismatches == expectedMismatches) return Orientation.Horizontal to row + 1
    }

    throw Exception("Should never reach this point, a mirror must exist!")
}

fun Pair<Orientation, Int>.summarize() =
    this.let { (orientation, index) ->
        when (orientation) {
            Orientation.Vertical -> index
            Orientation.Horizontal -> index * 100
        }
    }

// Part 1:

// To begin, can look for all pairs of identical and adjacent columns or rows. At least one such pair should exist,
// otherwise some mirrors would be on the edge, which would give multiple solutions. For each pair that's found, just
// check in both directions, ensuring that the rest of the reflection matches.

println(
    grids.sumOf { it.findMirror(expectedMismatches = 0).summarize() }
)

// Part 2:

// To find the smudge, basically modify the mirror-finding algorithm to keep track of the total number of mismatches
// across each tested reflection (instead of using an "is matched" boolean). If the mismatch is exactly 1, then that's
// the new mirror we're looking for. So as a small optimization, can just exit a particular check early if the number of
// mismatches goes beyond 1.

println(
    grids.sumOf { it.findMirror(expectedMismatches = 1).summarize() }
)
