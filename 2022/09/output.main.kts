import java.io.File

class Coordinates(var row: Int, var col: Int)

enum class Direction { U, D, L, R }

listOf(
    2, // Part 1
    10 // Part 2
).forEach { ropeLength ->
    val visitedByTail: MutableSet<Pair<Int, Int>> = mutableSetOf()
    val rope = List(ropeLength) { Coordinates(0, 0) }

    File("input.txt").readLines().forEach { line ->
        // Parse movement:
        val tokens = line.split(' ')
        val direction = Direction.valueOf(tokens[0])
        val distance = tokens[1].toInt()

        // Simulate movement:
        repeat(distance) {
            // Move head:
            when (direction) {
                Direction.U -> rope.first().row -= 1
                Direction.D -> rope.first().row += 1
                Direction.L -> rope.first().col -= 1
                Direction.R -> rope.first().col += 1
            }

            // Update the rest of the rope:
            for (i in 1 until rope.size) {
                when {
                    rope[i].row - rope[i - 1].row > 1 -> {
                        rope[i].row -= 1
                        when {
                            rope[i - 1].col < rope[i].col -> rope[i].col -= 1
                            rope[i - 1].col > rope[i].col -> rope[i].col += 1
                        }
                    }

                    rope[i - 1].row - rope[i].row > 1 -> {
                        rope[i].row += 1
                        when {
                            rope[i - 1].col < rope[i].col -> rope[i].col -= 1
                            rope[i - 1].col > rope[i].col -> rope[i].col += 1
                        }
                    }

                    rope[i].col - rope[i - 1].col > 1 -> {
                        rope[i].col -= 1
                        when {
                            rope[i - 1].row < rope[i].row -> rope[i].row -= 1
                            rope[i - 1].row > rope[i].row -> rope[i].row += 1
                        }
                    }

                    rope[i - 1].col - rope[i].col > 1 -> {
                        rope[i].col += 1
                        when {
                            rope[i - 1].row < rope[i].row -> rope[i].row -= 1
                            rope[i - 1].row > rope[i].row -> rope[i].row += 1
                        }
                    }
                }
            }

            // Mark the tail location as "visited":
            visitedByTail.add(Pair(rope.last().row, rope.last().col))
        }
    }

    // Count number of locations visited by the tail:
    println(visitedByTail.size)
}
