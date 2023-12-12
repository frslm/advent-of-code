import java.io.File

fun CharSequence.indexOfFirst(start: Int = 0, end: Int = this.length, predicate: (Char) -> Boolean): Int? {
    (start until end).forEach { if (predicate(this[it])) return it }
    return null
}

data class Record(val condition: String, val damagedGroupSizes: List<Int>) {
      companion object {
          const val OPERATIONAL = '.'
          const val DAMAGED = '#'
      }

    fun countPossibleArrangements(
        spring: Int = 0,
        group: Int = 0,
        cache: MutableMap<Pair<Int, Int>, Long> = mutableMapOf()
    ): Long {
        cache[spring to group]?.let { return it }

        var count = 0L

        damagedGroupSizes.getOrNull(group)?.let { groupSize ->
            var index = spring
            var operational = index

            while (index < condition.length) {
                // Exit if there's a damaged spring to the left:
                if (condition.indexOfFirst(operational, index) { it == DAMAGED } != null) break else { operational = index }

                // Exit if there are only operational springs to the right:
                val start = condition.indexOfFirst(index, condition.length - groupSize + 1) { it != OPERATIONAL } ?: break
                val end = start + groupSize

                // Recurse into the subsection if the current start-end group contains no operational springs and
                // no damaged spring comes after this group:
                val last = condition.indexOfFirst(start, end) { it == OPERATIONAL }
                    ?: start.also {
                        if (condition.getOrNull(end) != DAMAGED)
                            count += countPossibleArrangements(spring = end + 1, group = group + 1, cache)
                    }

                // Skip ahead to the next index to be processed (plus 1 to skip over the gap too):
                index = last + 1
            }
        } ?: condition.indexOfFirst(spring) { it == DAMAGED } ?: count++

        return count.also { cache[spring to group] = it }
    }
}

fun List<String>.parseRecords(expansionFactor: Int): List<Record> =
    this.map { line ->
        line.split(' ').let { (condition, damagedGroupSizes) ->
            Record(
                List(expansionFactor) { condition }.joinToString("?"),
                List(expansionFactor) { damagedGroupSizes }.joinToString(",").split(',').map { it.toInt() }
            )
        }
    }

val lines = File("input.txt").readLines()

// Part 1:

// Immediately thought of dynamic programming for this, though may not need memoization since the sub-problems don't
// seem to overlap much. So pick the first group size, then scan left-to-right for the first potential fit, recursively
// calling the same function on the remaining condition and group sizes.

println(
    lines.parseRecords(expansionFactor = 1).sumOf { it.countPossibleArrangements() }
)

// Part 2:

// Maybe now memoization makes sense; modify the function to support it, and change the `Int` return to a `Long`.

println(
    lines.parseRecords(expansionFactor = 5).sumOf { it.countPossibleArrangements() }
)
