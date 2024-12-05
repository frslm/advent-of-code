import java.io.File

val (rules, updates) = File("input.txt").readLines().let { lines ->
    lines.subList(0, lines.indexOf(""))
        .map { line -> line.split('|').let { (first, second) -> first.toInt() to second.toInt() } } to
    lines.subList(lines.indexOf("") + 1, lines.size)
        .map { line -> line.split(',').map { it.toInt() } }
}

fun List<Pair<Int, Int>>.filterRelevantTo(update: List<Int>): List<Pair<Int, Int>> =
    update.toSet().let { set ->
        this.filter { it.first in set && it.second in set }
    }

fun List<Pair<Int, Int>>.match(update: List<Int>): Boolean =
    this.all { update.indexOf(it.first) < update.indexOf(it.second) }

// Part 1:
println(
    updates
        .filter { rules.filterRelevantTo(it).match(it) }
        .sumOf { it[it.size / 2] }
)

// Part 2:
println(
    updates.mapNotNull { update ->
        val relevantRules = rules.filterRelevantTo(update)

        if (!relevantRules.match(update)) {
            val dependencies = update.associateWith { mutableSetOf<Int>() }.toMutableMap().apply {
                relevantRules.forEach { this[it.second]?.add(it.first) }
            }

            mutableListOf<Int>().apply {
                while (dependencies.isNotEmpty()) {
                    dependencies.filter { it.value.isEmpty() }.toList().single().first.let { page ->
                        this.add(page)
                        dependencies.remove(page)
                        dependencies.forEach { it.value.remove(page) }
                    }
                }
            }
        } else {
            null
        }
    }.sumOf { it[it.size / 2] }
)
