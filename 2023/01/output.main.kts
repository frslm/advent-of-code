import java.io.File

val lines = File("input.txt").readLines()

fun calibration(first: Char, last: Char) = "$first$last".toInt()

// Part 1:
println(
    lines.sumOf { line ->
        calibration(
            first = line.firstOrNull { it.isDigit() }!!,
            last = line.lastOrNull { it.isDigit() }!!
        )
    }
)

// Part 2:
val digits = mapOf(
    "1" to '1',
    "2" to '2',
    "3" to '3',
    "4" to '4',
    "5" to '5',
    "6" to '6',
    "7" to '7',
    "8" to '8',
    "9" to '9',
    "one" to '1',
    "two" to '2',
    "three" to '3',
    "four" to '4',
    "five" to '5',
    "six" to '6',
    "seven" to '7',
    "eight" to '8',
    "nine" to '9'
)

println(
    lines.sumOf { line ->
        calibration(
            first = digits
                .map { (string, digit) -> line.indexOf(string) to digit }
                .filter { (index, _) -> index != -1 }
                .minBy { (index, _) -> index }
                .let { (_, digit) -> digit },
            last = digits
                .map { (string, digit) -> line.lastIndexOf(string) to digit }
                .filter { (index, _) -> index != -1 }
                .maxBy { (index, _) -> index }
                .let { (_, digit) -> digit }
        )
    }
)
