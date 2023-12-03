import java.io.File

typealias Schematic = List<String>

fun Char.isSymbol() = !this.isDigit() && this != '.'

data class Cell(val row: Int, val column: Int, val schematic: Schematic) {
    val value get(): Char = schematic[row][column]
}

data class Number(val row: Int, val left: Int, val right: Int, val schematic: Schematic) {
    fun getSurroundingCells(): List<Cell> = listOfNotNull(
        /* (above) */ *(if (row > 0) (left .. right).map { Cell(row - 1, it, schematic) } else emptyList()).toTypedArray(),
        /* (below) */ *(if (row < schematic.size - 1) (left .. right).map { Cell(row + 1, it, schematic) } else emptyList()).toTypedArray(),
        /* (left) */  if (left > 0) Cell(row, left - 1, schematic) else null,
        /* (right) */ if (right < schematic.first().length - 1) Cell(row, right + 1, schematic) else null,
        /* (above-left) */ if (row > 0 && left > 0) Cell(row - 1, left - 1, schematic) else null,
        /* (below-left) */ if (row < schematic.size - 1 && left > 0) Cell(row + 1, left - 1, schematic) else null,
        /* (above-right) */ if (row > 0 && right < schematic.first().length - 1) Cell(row - 1, right + 1, schematic) else null,
        /* (below-right) */ if (row < schematic.size - 1 && right < schematic.first().length - 1) Cell(row + 1, right + 1, schematic) else null
    )

    fun isPartNumber(): Boolean = getSurroundingCells().any { it.value.isSymbol() }

    val value get(): Int = schematic[row].subSequence(left .. right).toString().toInt()
}

fun Schematic.getNumbers(): List<Number> =
    this.flatMapIndexed { row, line ->
        mutableListOf<Number>().also { numbers ->
            var left: Int? = null

            line.forEachIndexed { column, char ->
                if (char.isDigit()) {
                    left ?: run { left = column }
                } else {
                    left?.let { numbers.add(Number(row, it, column - 1, this)); left = null }
                }
            }

            left?.let { numbers.add(Number(row, it, line.length - 1, this)); left = null }
        }
    }

val partNumbers = File("input.txt").readLines().getNumbers().filter { it.isPartNumber() }

// Part 1:
println(partNumbers.sumOf { it.value })

// Part 2:
println(
    mutableMapOf<Cell, MutableList<Number>>().also { gearsWithAdjacentPartNumbers ->
        partNumbers.forEach { partNumber ->
            partNumber.getSurroundingCells().filter { it.value == '*' }.forEach { gear ->
                gearsWithAdjacentPartNumbers.getOrPut(gear) { mutableListOf() }.add(partNumber)
            }
        }
    }
        .values
        .filter { it.size == 2 }
        .sumOf { it.first().value * it.last().value }
)
