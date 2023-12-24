import java.io.File

data class Condition(val category: Char, val inequality: Char, val value: Long)
data class Rule(val condition: Condition?, val destination: String)

val lines = File("input.txt").readLines()

typealias Workflows = Map<String, List<Rule>>
val workflows: Workflows = lines.subList(0, lines.indexOf("")).associate { line ->
    Regex("([a-z]+)\\{(.*)\\}").matchEntire(line)!!.groups.let {
        it[1]!!.value to it[2]!!.value.split(",").map { rule ->
            Regex("(?:([xmas])([<>])(\\d+):)?([a-z]+|A|R)").matchEntire(rule)!!.groups.let { match ->
                Rule(
                    condition = match[1]?.value?.let { category ->
                        Condition(
                            category.toCharArray().single(),
                            inequality = match[2]!!.value.toCharArray().single(),
                            value = match[3]!!.value.toLong()
                        )
                    },
                    destination = match[4]!!.value
                )
            }
        }
    }
}

// Part 1:

data class Part(val x: Int, val m: Int, val a: Int, val s: Int)

fun Part.passes(condition: Condition): Boolean =
    when (condition.category) {
        'x' -> x
        'm' -> m
        'a' -> a
        's' -> s
        else -> throw Exception("Unknown category ${condition.category}!")
    }.let {
        when (condition.inequality) {
            '<' -> it < condition.value
            '>' -> it > condition.value
            else -> throw Exception("Unknown inequality ${condition.inequality}!")
        }
    }

val parts = lines.subList(lines.indexOf("") + 1, lines.size).map { line ->
    Regex("\\{x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)\\}").matchEntire(line)!!.groups.let {
        Part(
            it[1]!!.value.toInt(),
            it[2]!!.value.toInt(),
            it[3]!!.value.toInt(),
            it[4]!!.value.toInt()
        )
    }
}

println(
    parts.sumOf { part ->
        var rules = workflows["in"]!!
        var index = 0

        var accepted: Boolean? = null
        while (accepted == null) {
            val rule = rules[index]
            if (rule.condition == null || part.passes(rule.condition)) {
                when (rule.destination) {
                    "A" -> accepted = true
                    "R" -> accepted = false
                    else -> {
                        rules = workflows[rule.destination]!!
                        index = 0
                    }
                }
            } else {
                index++
            }
        }

        if (accepted) part.x + part.m + part.a + part.s else 0
    }
)

// Part 2:

// Start with a collection of all possible ranges for each rating, then at each condition, split the ranges at the
// appropriate rating and recurse each side appropriately:
// - For a passing condition, move on to the destination workflow and repeat.
// - For a failing condition, move on to the next rule in the set.
//
// The moment an 'A' is reached, multiply together the sizes of each rating's range to get the count of accepted
// combinations for that recursion branch. As for an 'R', just return 0 instead.

data class Range(val start: Long, val end: Long)
data class Ranges(private val ranges: Map<Char, Range>) {
    constructor(vararg ranges: Pair<Char, Range>) : this(ranges.toMap())

    private fun Map<Char, Range>.copy(modify: (MutableMap<Char, Range>) -> Unit) =
        this.toList().associate { it.first to it.second.copy() }.toMutableMap().also { modify(it) }

    fun splitAt(char: Char, value: Long): Pair<Ranges, Ranges> =
        Ranges(ranges.copy { it[char] = Range(it[char]!!.start, value) }) to
        Ranges(ranges.copy { it[char] = Range(value, it[char]!!.end) })

    fun countCombinations(): Long = ranges.values.fold(1L) { acc, range -> acc * (range.end - range.start) }
}

fun Workflows.countAcceptedCombinationsOf(
    ranges: Ranges,
    name: String,
    index: Int
): Long = when (name) {
    "A" -> ranges.countCombinations()
    "R" -> 0L
    else -> this[name]!![index].let { rule ->
        rule.condition?.run {
            when (inequality) {
                '<' -> ranges.splitAt(category, value)
                '>' -> ranges.splitAt(category, value + 1).let { it.second to it.first }
                else -> throw Exception("Unknown inequality $inequality!")
            }.let { (passing, failing) ->
                countAcceptedCombinationsOf(passing, rule.destination, index = 0) +
                countAcceptedCombinationsOf(failing, name, index = index + 1)
            }
        } ?: countAcceptedCombinationsOf(ranges, rule.destination, index = 0)
    }
}

println(
    workflows.countAcceptedCombinationsOf(
        ranges = Ranges(
            'x' to Range(1, 4001),
            'm' to Range(1, 4001),
            'a' to Range(1, 4001),
            's' to Range(1, 4001)
        ),
        name = "in",
        index = 0
    )
)
