import java.io.File

@Suppress("EnumEntryName")
enum class Operation { noop, addx }

fun processInput(perCycle: (Int, Int) -> Unit) {
    var register = 1
    var cycle = 0

    File("input.txt").forEachLine { line ->
        val fragments = line.split(' ')
        val onCycle = { cycle += 1; perCycle(register, cycle) }

        when (Operation.valueOf(fragments[0])) {
            Operation.noop -> onCycle()
            Operation.addx -> {
                repeat(2) { onCycle() }
                register += fragments[1].toInt()
            }
        }
    }
}

// Part 1:
val cycleCheckpoints = setOf(20, 60, 100, 140, 180, 220)

var signalStrength = 0
processInput { register, cycle -> if (cycle in cycleCheckpoints) signalStrength += cycle * register }
println(signalStrength)

// Part 2:
val outputWidth = 40

val output = mutableListOf(mutableListOf<Char>())
processInput { register, _ ->
    if (output.last().size >= outputWidth) output.add(mutableListOf())
    output.last().add(if (register - 1 <= output.last().size && output.last().size <= register + 1) '#' else '.')
}
output.forEach { println(it.joinToString()) }
