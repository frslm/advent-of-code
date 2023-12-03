import java.io.File

typealias WorryLevel = Long
typealias MonkeyIndex = Int

class Monkey(
    val items: MutableList<WorryLevel>,
    private val operation: (WorryLevel) -> WorryLevel,
    val divisor: Long,
    private val ifTrue: MonkeyIndex,
    private val ifFalse: MonkeyIndex
) {
    fun inspect(relief: (WorryLevel) -> WorryLevel): Pair<WorryLevel, MonkeyIndex> =
        (operation(relief(items.removeFirst()))).let { Pair(it, if (it % divisor == 0L) ifTrue else ifFalse) }
}

val noteLength = 7

fun getMonkeys(): List<Monkey> {
    val lines = File("input.txt").readLines()

    return (0 until (lines.size + 1) / noteLength).map { index ->
        val match: Regex.(Int) -> MatchGroupCollection = { offset ->
            matchEntire(lines[index * noteLength + offset])?.groups ?: throw Exception("Unexpected format!")
        }

        val group: MatchGroupCollection.(Int) -> String = { group ->
            get(group)?.value ?: throw Exception("Unexpected format!")
        }

        Monkey(
            items = Regex("  Starting items: (.*)").match(1).group(1).split(", ").map { it.toLong() }.toMutableList(),
            operation = Regex("  Operation: new = old (.) (.*)").match(2).let {
                it.group(2).toLongOrNull().let { value ->
                    when (it.group(1)) {
                        "*" -> { item -> item * (value ?: item) }
                        "+" -> { item -> item + (value ?: item) }
                        else -> throw Exception("Unknown operator!")
                    }
                }
            },
            divisor = Regex("  Test: divisible by (\\d+)").match(3).group(1).toLong(),
            ifTrue = Regex("    If true: throw to monkey (\\d+)").match(4).group(1).toInt(),
            ifFalse = Regex("    If false: throw to monkey (\\d+)").match(5).group(1).toInt()
        )
    }
}

fun List<Monkey>.monkeyAround(repeat: Int, relief: (WorryLevel) -> WorryLevel): Long {
    val inspections = MutableList(this.size) { 0L }

    repeat(repeat) {
        this.forEachIndexed { index, monkey ->
            while (monkey.items.isNotEmpty()) {
                val (item, receivingMonkey) = monkey.inspect(relief)
                inspections[index] += 1L
                this[receivingMonkey].items += item
            }
        }
    }

    return inspections.also { it.sort() }.takeLast(2).reduce { acc, num -> acc * num }
}

// Part 1:
println(getMonkeys().monkeyAround(repeat = 20, relief = { it / 3 }))

// Part 2:
getMonkeys().let { monkeys ->
    val leastCommonMultiple = monkeys.map { it.divisor }.reduce { acc, num -> acc * num }
    println(monkeys.monkeyAround(repeat = 10000, relief = { it % leastCommonMultiple }))
}

//    From example...
//    Tests divide by: 23, 19, 13, 17
//
//    Base case: We store the values as-is; will always work until the numbers overflow.
//    Goal: Want to store much smaller values that still pass all the tests.
//
//    If X = 230 (23*10), then it'll pass 230%23 (10r0), but none of the others, so an equivalent number would be 230%23 = 0.
//    We don't care about the quotient, only the remainder, which must remain constant.
//
//    Now what about X = 23*19 = 437? it'll pass the 23 and 19 tests (r0) but not 13 (r8) and 17 (r12).
//    But what would an equivalent number be in this case?
//    If 437%23 = 0 or 437%19 = 0, then it'll pass 13 and 17 too.
//
//    Basically, we have...
//    X%23 = Ar0
//    X%19 = Br0
//    X%13 = Cr8
//    X%17 = Dr12
//    ...we want to reduce X to Y in such a way that...
//    Y%23 = Er0
//    Y%19 = Fr0
//    Y%13 = Gr8
//    Y%17 = Hr12
//    ...where Y < X but all numbers are still integers.
//
//    Taking the %23 case, X%23 = Ar0 and Y%23 = Er0 means that 23A = X and 23E = Y, so X and Y should be multiples of 23.
//    Similarly, for %19, X and Y should also be multiples of 19.
//    So, so far, Y = 23a * 19b = 23 * 19 * ab = 23 * 19 * c, where c is an integer.
//
//    But, Y % 13 should still return 8, so we can simply loop through all values of c from 0 until we find a match
//    Then we do the same for Y % 17 and loop for all such values until the remainder for that returns 12
//
//    Can we find a pattern to avoid this looping though?
//
//    437 * 1 % 13 = 8
//    437 * 2 % 13 = 3
//    437 * 3 % 13 = 11
//    437 * 4 % 13 = 6
//    437 * 5 % 13 = 1
//    437 * 6 % 13 = 9
//    437 * 7 % 13 = 4
//    437 * 8 % 13 = 12
//    437 * 9 % 13 = 7
//    437 * 10 % 13 = 2
//    437 * 11 % 13 = 10
//    437 * 12 % 13 = 5
//    437 * 13 % 13 = 0
//
//    437 * (1+13) % 13 = 8
//    ...
//
//    437 * (1 + (0*13)) % 17 = 12
//    437 * (1 + (1*13)) % 17 = 15
//    ...
//    437 * (1 + (17*13)) % 17 = 12 (number is 97014 here)
//
//    ---
//
//    Arbitrary example:
//    23=1, 19=2, 13=3, 17=4
//
//    0 % 17 = 0
//    ...
//    4 % 17 = 4
//
//    (4 + (0 * 17)) % 13 = 4 // each step adds 17-13
//    ...
//    (4 + (3 * 17)) % 13 = 3
//
//    (4 + (3 * 17) + (0 * 17 * 13)) % 19 = 17 // each step adds 17 * 13 - 19 = 202 (%19 = 12)
//    (4 + (3 * 17) + (1 * 17 * 13)) % 19 = 10
//    ...
//    (4 + (3 * 17) + (13 * 17 * 13)) % 19 = 2
//
//    (4 + (3 * 17) + (13 * 17 * 13) + (0 * 17 * 13 * 19)) % 23 = 7
//    ...
//    (4 + (3 * 17) + (13 * 17 * 13) + (19 * 17 * 13 * 19) + (x * 17 * 13 * 19 * 23)) % 23 = 1
//
//    number = 82709
//
//    ---
//
//    82709  = (4 + 17 * (3 + 13 * (13 + (19 * (19 + (23 * 0)))))) = (4 + (3 * 17) + (13 * 17 * 13) + (19 * 17 * 13 * 19) + (0 * 17 * 13 * 19 * 23))
//    179286 = (4 + 17 * (3 + 13 * (13 + (19 * (19 + (23 * 1)))))) = (4 + (3 * 17) + (13 * 17 * 13) + (19 * 17 * 13 * 19) + (1 * 17 * 13 * 19 * 23))
//
//    So do we just multiply all the divisors then subtract that from a large number repeatedly?
//    Since factor is 96577, then 179286 % 96577 = 82709
