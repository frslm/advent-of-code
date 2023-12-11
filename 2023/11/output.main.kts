import java.io.File
import kotlin.math.abs

val lines = File("input.txt").readLines()

fun List<List<Char>>.getEmptyRows(): List<Int> =
    this.mapIndexedNotNull { row, pixels ->
        if (pixels.all { it == '.' }) row else null
    }

fun List<List<Char>>.getEmptyCols(): List<Int> =
    (0 until this.first().size).mapNotNull { col ->
        if (this.indices.map { this[it][col] }.all { it == '.' }) col else null
    }

fun <R> List<List<Char>>.getGalaxies(locationTransform: (Int, Int) -> Pair<R, R>): List<Pair<R, R>> =
    this.flatMapIndexed { row, pixels ->
        pixels.mapIndexedNotNull { col, pixel ->
            if (pixel == '#') locationTransform(row, col) else null
        }
    }

fun Pair<Long, Long>.manhattanDistanceTo(other: Pair<Long, Long>) =
    abs(other.first - this.first) + abs(other.second - this.second)

fun List<Pair<Long, Long>>.calculatePairDistances(): List<Long> =
    this.indices.flatMap { left ->
        (left + 1 until this.size).map { right ->
            this[left].manhattanDistanceTo(this[right])
        }
    }

// Part 1:

// Physically expand the grid by adding empty rows and columns.

println(
    lines.map { it.toMutableList() }.toMutableList()
        .let { grid ->
            grid.getEmptyRows()
                .reversed() // (so that additions don't invalidate the remaining indices)
                .forEach { grid.add(it, MutableList(grid.first().size) { '.' }) }

            grid.getEmptyCols()
                .reversed() // (so that additions don't invalidate the remaining indices)
                .let { emptyCols -> grid.forEach { pixels -> emptyCols.forEach { pixels.add(it, '.') } } }

            grid.getGalaxies { row, col -> row.toLong() to col.toLong() }
        }
        .calculatePairDistances().sum()
)

// Part 2:

// So instead of physically expanding the grid, use a new set of indices that account for any space expansion.

fun IntRange.withExpansion(emptyIndices: Set<Int>, expansionFactor: Long): List<Long> {
    var expansionSoFar = 0L
    return this.map { index ->
        (index + expansionSoFar).also {
            if (index in emptyIndices) expansionSoFar += expansionFactor
        }
    }
}

println(
    lines.map { it.toList() }
        .let { grid ->
            val rowIndices = grid.indices.withExpansion(grid.getEmptyRows().toSet(), 999_999L)
            val colIndices = grid.first().indices.withExpansion(grid.getEmptyCols().toSet(), 999_999L)

            grid.getGalaxies { row, col -> rowIndices[row] to colIndices[col] }
        }
        .calculatePairDistances().sum()
)
