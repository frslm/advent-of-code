import java.io.File
import java.util.Objects.hash

data class Blueprint(
    val id: Int,
    val oreRobotOreCost: Int,
    val clayRobotOreCost: Int,
    val obsidianRobotOreCost: Int,
    val obsidianRobotClayCost: Int,
    val geodeRobotOreCost: Int,
    val geodeRobotObsidianCost: Int,
)

data class Inventory(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geodes: Int = 0,
    val oreRobots: Int = 0,
    val clayRobots: Int = 0,
    val obsidianRobots: Int = 0,
    val geodeRobots: Int = 0,
) {
    operator fun plus(other: Inventory) = Inventory(
        ore = ore + other.ore,
        clay = clay + other.clay,
        obsidian = obsidian + other.obsidian,
        geodes = geodes + other.geodes,
        oreRobots = oreRobots + other.oreRobots,
        clayRobots = clayRobots + other.clayRobots,
        obsidianRobots = obsidianRobots + other.obsidianRobots,
        geodeRobots = geodeRobots + other.geodeRobots,
    )

    override fun equals(other: Any?): Boolean = other is Inventory &&
            ore == other.ore &&
            clay == other.clay &&
            obsidian == other.obsidian &&
            geodes == other.geodes &&
            oreRobots == other.oreRobots &&
            clayRobots == other.clayRobots &&
            obsidianRobots == other.obsidianRobots &&
            geodeRobots == other.geodeRobots

    override fun hashCode(): Int = hash(ore, clay, obsidian, geodes, oreRobots, clayRobots, obsidianRobots, geodeRobots)
}

fun Blueprint.maxGeodes(
    minutesRemaining: Int,
    inventory: Inventory,
    memory: MutableMap<Pair<Int, Inventory>, Int> = mutableMapOf()
): Int =
    when (minutesRemaining) {
        0 -> inventory.geodes
        else -> {
            //if (Pair(minutesRemaining, inventory) !in memory) {
            val possibleBuilds = listOfNotNull( // (assumes that we'd only ever have enough for one build at a time)
                if (inventory.ore >= oreRobotOreCost)
                    Inventory(ore = -oreRobotOreCost, oreRobots = +1)
                else null,

                if (inventory.ore >= clayRobotOreCost)
                    Inventory(ore = -clayRobotOreCost, clayRobots = +1)
                else null,

                if (inventory.ore >= obsidianRobotOreCost && inventory.clay >= obsidianRobotClayCost)
                    Inventory(ore = -obsidianRobotOreCost, clay = -obsidianRobotClayCost, obsidianRobots = +1)
                else null,

                if (inventory.ore >= geodeRobotOreCost && inventory.obsidian >= geodeRobotObsidianCost)
                    Inventory(ore = -geodeRobotOreCost, obsidian = -geodeRobotObsidianCost, geodeRobots = +1)
                else null,

                Inventory() // (build nothing)
            )

            val newInventory = inventory + Inventory(
                ore = inventory.oreRobots,
                clay = inventory.clayRobots,
                obsidian = inventory.obsidianRobots,
                geodes = inventory.geodeRobots,
            )

            // memory[Pair(minutesRemaining, inventory)] =
            possibleBuilds.maxOfOrNull { this.maxGeodes(minutesRemaining - 1, newInventory + it, memory) }!!
            //}

            //memory[Pair(minutesRemaining, inventory)]!!
            // TODO: Memoization didn't work since it hit the heap limit for the second example blueprint...
            //       Maybe I need to memoize somewhat differently to reduce the total number of permutations?
        }
    }


val group: MatchGroupCollection.(Int) -> Int = { get(it)?.value?.toInt() ?: throw Exception("Unexpected format!") }

val blueprints = File("input.txt").readLines().map { line ->
    val groups =
        Regex("Blueprint (\\d+): Each ore robot costs (\\d+) ore\\. Each clay robot costs (\\d+) ore\\. Each obsidian robot costs (\\d+) ore and (\\d+) clay\\. Each geode robot costs (\\d+) ore and (\\d+) obsidian\\.")
            .matchEntire(line)?.groups
            ?: throw Exception("Unexpected format!")

    Blueprint(
        id = groups.group(1),
        oreRobotOreCost = groups.group(2),
        clayRobotOreCost = groups.group(3),
        obsidianRobotOreCost = groups.group(4),
        obsidianRobotClayCost = groups.group(5),
        geodeRobotOreCost = groups.group(6),
        geodeRobotObsidianCost = groups.group(7),
    )
}

// Part 1:

// Seems like a knapsack-like problem; at each minute, list out all the possible actions that can be taken, then
// recurse through each of those actions.

println(blueprints.last().maxGeodes(24, Inventory(oreRobots = 1)))

println(blueprints.sumOf { it.id * it.maxGeodes(24, Inventory(oreRobots = 1)) })

// Part 2:
println() // TODO
