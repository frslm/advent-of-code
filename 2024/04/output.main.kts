import java.io.File

val lines = File("input.txt").readLines()

typealias Row = Int
typealias Col = Int

private fun List<String>.find(
    target: String,
    start: Pair<Row, Col>,
    vector: Pair<Row, Col>
): Boolean {
    target.forEachIndexed { index, char ->
        if (
            this
                [(start.first + index * vector.first).also { if (it < 0 || it >= this.size) return false }]
                [(start.second + index * vector.second).also { if (it < 0 || it >= this.first().length) return false }]
            != char
        ) {
            return false
        }
    }

    return true
}

// Part 1:
println(
    lines.flatMapIndexed { row, line ->
        line.indices.flatMap { col ->
            setOf(-1 to -1, 0 to -1, 1 to -1, -1 to 0, 1 to 0, -1 to 1, 0 to 1, 1 to 1).map {
                lines.find("XMAS", row to col, it)
            }
        }
    }.count { it }
)

// Part 2:
println(
    lines.flatMapIndexed { row, line ->
        line.indices.map { col ->
            fun check(target: String, vector: Pair<Row, Col>) = lines.find(target, row to col, vector)

            (
                (check("AS", -1 to -1) && check("AM", 1 to 1)) ||
                (check("AM", -1 to -1) && check("AS", 1 to 1))
            ) && (
                (check("AS", -1 to 1) && check("AM", 1 to -1)) ||
                (check("AM", -1 to 1) && check("AS", 1 to -1))
            )
        }
    }.count { it }
)
