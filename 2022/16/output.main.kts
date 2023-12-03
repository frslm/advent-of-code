import java.io.File
import java.lang.Integer.max

typealias Label = String

typealias Pressure = Int
typealias Minute = Int
typealias PressurePerMinute = Int

class Valve(val flowRate: PressurePerMinute, val connections: List<Label>, var open: Boolean = false)

typealias Valves = Map<Label, Valve>

fun Valves.find(label: Label): Valve = this[label] ?: throw Exception("Couldn't find node $label")

class Path(val to: Valve, val from: Valve? = null)

fun Valves.maxPressure(minutesRemaining: Minute, path: Path): Pressure =
    when (minutesRemaining) {
        0 -> 0
        else -> max(
            when {
                path.to.flowRate != 0 && !path.to.open -> (minutesRemaining - 1).let {
                    path.to.open = true
                    (path.to.flowRate * it + maxPressure(it, Path(path.to))).also {
                        path.to.open = false
                    }
                }

                else -> 0
            },
            path.to.connections.map { this.find(it) }
                .filter { it != path.from } // (filter out previous node to avoid bouncing back and forth)
                .maxOfOrNull { maxPressure(minutesRemaining - 1, Path(it, path.to)) } ?: 0
        )
    }

fun Valves.maxPressureWithHelp(minutesRemaining: Minute, path1: Path, path2: Path): Pressure =
    when (minutesRemaining) {
        0 -> 0
        else -> (listOf(null) + path1.to.connections).flatMap { connection1 ->
            (listOf(null) + path2.to.connections).map { connection2 ->
                Pair(connection1, connection2)
            }
        }.maxOfOrNull { (connection1, connection2) ->
            when {
                connection1 != null && connection2 != null ->
                    listOf(path1, path2).map { it.from }.let { from ->
                        when {
                            listOf(
                                this.find(connection1),
                                this.find(connection2)
                            ).none { it in from } -> maxPressureWithHelp(
                                // Assumption: Tried to optimize by not backtracking if we came from _at least one_ room.
                                // Can lessen this by requiring that both don't backtrack, or by removing this optimization completely.
                                minutesRemaining - 1,
                                Path(this.find(connection1), path1.to),
                                Path(this.find(connection2), path2.to)
                            )

                            else -> 0
                        }
                    }

                connection1 != null && connection2 == null -> {
                    val valvesJustOpened = mutableListOf<Valve>()

                    val newPressure = listOf(path2.to).sumOf { valve ->
                        if (valve.flowRate != 0 && !valve.open) {
                            valve.open = true
                            valvesJustOpened += valve
                            valve.flowRate * (minutesRemaining - 1)
                        } else {
                            0
                        }
                    }

                    if (newPressure > 0) {
                        newPressure + maxPressureWithHelp(
                            minutesRemaining - 1,
                            Path(this.find(connection1), path1.to),
                            Path(path2.to)
                        )
                    } else {
                        0 // (if no new valves were opened, then we just wasted a turn here, so don't bother recursing)
                    }.also { valvesJustOpened.forEach { it.open = false } }
                }

                connection1 == null && connection2 != null -> {
                    val valvesJustOpened = mutableListOf<Valve>()

                    val newPressure = listOf(path1.to).sumOf { valve ->
                        if (valve.flowRate != 0 && !valve.open) {
                            valve.open = true
                            valvesJustOpened += valve
                            valve.flowRate * (minutesRemaining - 1)
                        } else {
                            0
                        }
                    }

                    if (newPressure > 0) {
                        newPressure + maxPressureWithHelp(
                            minutesRemaining - 1,
                            Path(path1.to),
                            Path(this.find(connection2), path2.to)
                        )
                    } else {
                        0 // (if no new valves were opened, then we just wasted a turn here, so don't bother recursing)
                    }.also { valvesJustOpened.forEach { it.open = false } }
                }

                else -> {
                    assert(connection1 == null && connection2 == null)

                    if (path1.to == path2.to) {
                        // Assumption: Tried to optimize by skipping cases where both characters are on the same
                        // valve and try to open it.
                        0
                    } else {
                        val valvesJustOpened = mutableListOf<Valve>()

                        val newPressure = listOf(path1.to, path2.to).sumOf { valve ->
                            if (valve.flowRate != 0 && !valve.open) {
                                valve.open = true
                                valvesJustOpened += valve
                                valve.flowRate * (minutesRemaining - 1)
                            } else {
                                0
                            }
                        }

                        if (newPressure > 0) {
                            newPressure + maxPressureWithHelp(minutesRemaining - 1, Path(path1.to), Path(path2.to))
                        } else {
                            0 // (if no new valves were opened, then we just wasted a turn here, so don't bother recursing)
                        }.also { valvesJustOpened.forEach { it.open = false } }
                    }
                }
            }
        } ?: 0
    }

val group: MatchGroupCollection.(Int) -> String = { get(it)?.value ?: throw Exception("Unexpected format!") }
// TODO: Pull out Regex functions into util.

val valves: Valves = File("input.txt").readLines().associate { line ->
    val groups = Regex("Valve ([A-Z]+) has flow rate=(\\d+); tunnels? leads? to valves? ([A-Z, ]+)")
        .matchEntire(line)?.groups
        ?: throw Exception("Unexpected format!")

    groups.group(1) to Valve(
        flowRate = groups.group(2).toInt(),
        connections = groups.group(3).split(", ")
    )
}

// Part 1:

// First build a graph of the tunnels connecting the valves, then perhaps recurse through it?
// So at a given room, I can either open the current valve then check the connected tunnels, or directly check the
// connected tunnels, returning the max pressure out of all those cases. I'd need to pass in the current time, as
// well as a set of unopened valves. To optimize, I can memoize the current room and time and set of unopened
// valves? We'll see if it's even needed...

println(valves.maxPressure(30, Path(valves.find("AA"))))

// Part 2:

// Now we have two entities traversing and modifying the same tree. We could modify the traversal to search all
// paths as in the first part, and if both entities hit the same point, then one of them can't touch the valve if
// the other already opened it. I wonder how the time complexity for this approach will turn out... Cause now at
// each branch, we investigate connections^2 paths. We can avoid the duplicated half of permutations, but it's still
// O(connections^2) at each branch. We could memoize, but then I'd need to store: minutes, positions of both actors,
// yet-to-be-opened valves.

println(valves.maxPressureWithHelp(26, Path(valves.find("AA")), Path(valves.find("AA"))))
// TODO: Takes too long even on the example input; need to figure out if the approach is stuck in a loop, and if not, how to further optimize this.
