import java.io.File

val steps = File("input.txt").readLines().first().split(',')

fun String.hash(): Int {
    var value = 0

    this.forEach {
        value += it.code
        value *= 17
        value %= 256
    }

    return value
}

// Part 1:
println(
    steps.sumOf { it.hash() }
)

// Part 2:
data class Lens(val label: String, val focalLength: Int)

fun List<MutableList<Lens>>.installLensesFollowing(steps: List<String>) {
    steps.forEach { step ->
        Regex("([a-z]+)(.*)").matchEntire(step)!!.groups.let { match ->
            val label = match[1]!!.value
            val box = this[label.hash()]

            Regex("=(\\d)").matchEntire(match[2]!!.value)?.groups?.get(1)?.value?.toInt()?.let { focalLength ->
                val lens = Lens(label, focalLength)
                when (val index = box.indexOfFirst { it.label == label }) {
                    -1 -> box.add(lens)
                    else -> box[index] = lens
                }
            } ?: box.removeIf { it.label == label }
        }
    }
}

println(
    List(256) { mutableListOf<Lens>() }
        .also { it.installLensesFollowing(steps) }
        .flatMapIndexed { index, box ->
            box.mapIndexed { slot, lens ->
                (1 + index) * (1 + slot) * lens.focalLength
            }
        }.sum()
)
