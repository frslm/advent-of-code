import java.io.File

val lines = File("input.txt").readLines()

private fun MatchResult.mul() =
    this.groupValues[1].toInt() * this.groupValues[2].toInt()

// Part 1:
println(
    lines.sumOf { line ->
        Regex("mul\\((\\d{1,3}),(\\d{1,3})\\)").findAll(line).map { it.mul() }.sum()
    }
)

// Part 2:
var enabled = true
var result = 0

enum class Operation(val regex: Regex) {
    MUL(Regex("mul\\((\\d{1,3}),(\\d{1,3})\\)")),
    DO(Regex("do\\(\\)")),
    DONT(Regex("don't\\(\\)"))
}

lines.forEach { line ->
    var index = 0

    while (true) {
        val (operation, match) =
            Operation.entries
                .mapNotNull { operation -> operation.regex.find(line, index)?.let { operation to it } }
                .sortedBy { (_, match) -> match.range.first }
                .firstOrNull()
                ?: break

        when (operation) {
            Operation.MUL -> if (enabled) result += match.mul()
            Operation.DO -> enabled = true
            Operation.DONT -> enabled = false
        }

        index = match.range.last
    }
}

println(result)
