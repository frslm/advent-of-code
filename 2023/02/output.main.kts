import java.io.File

typealias Id = Int
typealias Colour = String
typealias Set = List<Pair<Int, Colour>>
typealias Game = Pair<Id, List<Set>>

val games: List<Game> = File("input.txt").readLines().map { line ->
    Regex("Game (\\d+): (.*)").matchEntire(line)!!.groups.let { matches ->
        matches[1]!!.value.toInt() to
        matches[2]!!.value.split("; ").map { set ->
            set.split(", ").map { cube ->
                cube.split(" ").let { (amount, colour) -> amount.toInt() to colour }
            }
        }
    }
}

// Part 1:
val maxes = mapOf(
    "red" to 12,
    "green" to 13,
    "blue" to 14
)

println(
    games.sumOf { (id, sets) ->
        val withinCapacity = sets.all { set ->
            set.all { cube ->
                cube.let { (amount, colour) ->
                    amount <= (maxes[colour] ?: 0)
                }
            }
        }

        if (withinCapacity) id else 0
    }
)

// Part 2:
println(
    games.sumOf { (_, sets) ->
        val amountsRequired = mutableMapOf<Colour, Int>()

        sets.forEach { set ->
            set.forEach { cube ->
                cube.let { (amount, colour) ->
                    amountsRequired[colour] = (amountsRequired[colour] ?: 0).coerceAtLeast(amount)
                }
            }
        }

        amountsRequired.values.toList().reduce { acc, amount -> acc * amount }
    }
)
