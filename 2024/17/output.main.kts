import java.io.File
import kotlin.math.pow

data class Device(
    private var registerA: Long,
    private var registerB: Long,
    private var registerC: Long,
    val program: List<Int>
) {
    private fun literalOperand(pointer: Int) = program[pointer + 1]
    private fun comboOperand(pointer: Int) = when (val operand = program[pointer + 1]) {
        0, 1, 2, 3 -> operand.toLong()
        4 -> registerA
        5 -> registerB
        6 -> registerC
        7 -> throw Exception("Invalid combo operand!")
        else -> throw Exception("Unknown operand!")
    }

    fun run(): List<Int> {
        val output = mutableListOf<Int>()

        var pointer = 0
        while (pointer < program.size) {
            when (program[pointer]) {
                0 -> {
                    registerA /= 2.toDouble().pow(comboOperand(pointer).toDouble()).toLong()
                    pointer += 2
                }

                1 -> {
                    registerB = registerB xor literalOperand(pointer).toLong()
                    pointer += 2
                }

                2 -> {
                    registerB = comboOperand(pointer) % 8
                    pointer += 2
                }

                3 -> when (registerA) {
                    0L -> pointer += 2
                    else -> pointer = literalOperand(pointer)
                }

                4 -> {
                    registerB = registerB xor registerC
                    pointer += 2
                }

                5 -> {
                    output.add((comboOperand(pointer) % 8).toInt())
                    pointer += 2
                }

                6 -> {
                    registerB = registerA / 2.toDouble().pow(comboOperand(pointer).toDouble()).toLong()
                    pointer += 2
                }

                7 -> {
                    registerC = registerA / 2.toDouble().pow(comboOperand(pointer).toDouble()).toLong()
                    pointer += 2
                }
            }
        }

        return output
    }
}

val device = File("input.txt").readLines().let { (a, b, c, _, program) ->
    Device(
        registerA = Regex("Register A: (\\d+)").matchEntire(a)!!.groupValues[1].toLong(),
        registerB = Regex("Register B: (\\d+)").matchEntire(b)!!.groupValues[1].toLong(),
        registerC = Regex("Register C: (\\d+)").matchEntire(c)!!.groupValues[1].toLong(),
        program = Regex("Program: (.*)").matchEntire(program)!!.groupValues[1].split(',').map { it.toInt() }
    )
}

// Part 1:
println(
    device.copy().run().joinToString(",")
)

// Part 2:

// Brute force won't work and modulos usually mean you can't work backwards to find a closed-form solution.
//
// Can try to see if the same output appears again for a higher value of register A?
// No, there's no clear pattern if the outputs bucketed according to their hash.
//
// What if the outputs for each iteration were just printed?
// Now seeing a pattern; every output is later repeated in a contiguous block of 8 elements, where all but the first
// value match the output. So we could use this to traverse the search space (feels a bit like navigating down a binary
// heap, but using base 8).

var result: Long? = null

val candidates = (1L until 8L).toMutableList()
while (candidates.isNotEmpty()) {
    val candidate = candidates.take(1).single().also { candidates.removeAt(0) }
    when (val output = device.copy(registerA = candidate).run()) {
        device.program -> { result = candidate; break }
        device.program.takeLast(output.size) -> candidates.addAll((0L until 8L).map { it + candidate * 8 })
    }
}

println(
    result
)
