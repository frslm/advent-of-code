import java.io.File
import kotlin.math.pow

val cards = File("input.txt").readLines().map { line ->
    Regex("Card +\\d+:(.*?)\\|(.*?)").matchEntire(line)!!.groups.let { matches ->
        matches[1]!!.value.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }.toSet() to
        matches[2]!!.value.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }
    }
}

// Part 1:
println(
    cards.sumOf { (winningNumbers, numbersInHand) ->
        2.0.pow(numbersInHand.count { it in winningNumbers } - 1).toInt()
    }
)

// Part 2:
println(
    MutableList(cards.size) { 1 }.also { copiesPerCard ->
        cards.forEachIndexed { index, (winningNumbers, numbersInHand) ->
            (0 until numbersInHand.count { it in winningNumbers }).forEach {
                copiesPerCard[index + 1 + it] += copiesPerCard[index]
            }
        }
    }.sum()
)
