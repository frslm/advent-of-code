import java.io.File

typealias Vector = Pair<Long, Long>
operator fun Vector.plus(vector: Vector): Vector = first + vector.first to second + vector.second
operator fun Vector.minus(vector: Vector) = first - vector.first to second - vector.second

val MatchResult.vector get(): Vector =
    this.groupValues[1].toLong() to this.groupValues[2].toLong()

typealias Tokens = Long
val NO_WAY_TO_WIN: Tokens = -1

data class Button(
    val move: Vector,
    val cost: Tokens
)

data class Machine(
    val buttons: List<Button>,
    val prize: Vector
)

val machines =
    File("input.txt")
        .readLines()
        .fold(mutableListOf(mutableListOf<String>())) { group, line ->
            group.apply { if (line.isEmpty()) add(mutableListOf()) else last().add(line) }
        }
        .mapNotNull { (buttonA, buttonB, prize) ->
            Regex("Prize: X=(\\d+), Y=(\\d+)").matchEntire(prize)?.let { machineMatch ->
                Machine(
                    buttons = listOfNotNull(
                        Regex("Button A: X\\+(\\d+), Y\\+(\\d+)").matchEntire(buttonA)?.let {
                            Button(move = it.vector, cost = 3)
                        },
                        Regex("Button B: X\\+(\\d+), Y\\+(\\d+)").matchEntire(buttonB)?.let {
                            Button(move = it.vector, cost = 1)
                        }
                    ),
                    prize = machineMatch.vector
                )
            }
        }

// Part 1:
fun Machine.fewestTokensToWinPrize(): Tokens {
    fun playTowards(prize: Vector, history: MutableMap<Vector, Tokens> = mutableMapOf()): Tokens =
        history[prize] ?: when {
            prize.first == 0L && prize.second == 0L -> 0
            prize.first < 0L || prize.second < 0L -> NO_WAY_TO_WIN
            else -> this.buttons
                .map { button -> playTowards(prize - button.move, history) to button.cost }
                .filter { it.first != NO_WAY_TO_WIN }
                .minOfOrNull { it.first + it.second }
                ?: NO_WAY_TO_WIN
        }.also {
            history[prize] = it
        }

    return playTowards(this.prize)
}

println(
    machines.map { it.fewestTokensToWinPrize() }.filter { it != NO_WAY_TO_WIN }.sum()
)

// Part 2:

// System of equations with two unknown variables; calculate the unknowns but only take them if they're both integers.

val fixedMachines = machines.map { machine ->
    machine.copy(prize = machine.prize + (10000000000000L to 10000000000000L))
}

infix fun Long.integerDivide(divisor: Long): Long? =
    when {
        this % divisor == 0L -> this / divisor
        else -> null
    }

println(
    fixedMachines.mapNotNull { machine ->
        (
            (
                machine.prize.first * machine.buttons.last().move.second -
                machine.prize.second * machine.buttons.last().move.first
            ) integerDivide (
                machine.buttons.first().move.first * machine.buttons.last().move.second -
                machine.buttons.last().move.first * machine.buttons.first().move.second
            )
        )?.let { a ->
            (
                (
                    machine.prize.first - machine.buttons.first().move.first * a
                ) integerDivide (
                    machine.buttons.last().move.first
                )
            )?.let { b ->
                a * machine.buttons.first().cost + b * machine.buttons.last().cost
            }
        }
    }.sum()
)
