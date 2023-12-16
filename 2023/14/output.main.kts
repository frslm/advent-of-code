import java.io.File

val grid = File("input.txt").readLines().map { it.toMutableList() }.toMutableList()

fun MutableList<MutableList<Char>>.tiltVertical(north: Boolean) {
    (0 until this.first().size).forEach { col ->
        var free: Int? = null
        (0 until this.size)
            .let { if (north) it else it.reversed() }
            .forEach { row ->
            when (this[row][col]) {
                '.' -> free ?: run { free = row }
                'O' -> free?.let {
                    this[it][col] = 'O'
                    this[row][col] = '.'
                    free = it + if (north) 1 else -1
                }
                '#' -> free = null
            }
        }
    }
}

fun List<List<Char>>.calculateLoad() =
    this.mapIndexed { row, spaces -> spaces.count { it == 'O' } * (this.size - row) }.sum()

// Part 1:
println(
    grid
        .also { it.tiltVertical(north = true) }
        .calculateLoad()
)

// Part 2:

// There are probably only so many patterns before they start repeating, so try hashing the grid after each cycle and
// checking against any previously-seen hashes. Once there's a match, count up a multiple of cycle periods from that
// last-seen hash, then run the last few cycles normally until the target limit is reached.

fun MutableList<MutableList<Char>>.tiltHorizontal(west: Boolean) {
    (0 until this.size).forEach { row ->
        var free: Int? = null
        (0 until this.first().size)
            .let { if (west) it else it.reversed() }
            .forEach { col ->
                when (this[row][col]) {
                    '.' -> free ?: run { free = col }
                    'O' -> free?.let {
                        this[row][it] = 'O'
                        this[row][col] = '.'
                        free = it + if (west) 1 else -1
                    }
                    '#' -> free = null
                }
            }
    }
}

fun MutableList<MutableList<Char>>.spinCycle() {
    this.tiltVertical(north = true)
    this.tiltHorizontal(west = true)
    this.tiltVertical(north = false)
    this.tiltHorizontal(west = false)
}

typealias Cycle = Int

fun MutableList<MutableList<Char>>.spinUntil(limit: Int) {
    val previouslySeenHashes = mutableMapOf<Int, Cycle>()
    var cycle: Cycle = 0
    while (cycle < limit) {
        grid.spinCycle()
        cycle++

        val hash = grid.hashCode()
        previouslySeenHashes[hash]?.let {
            val remaining = limit - cycle
            val period = cycle - it

            cycle += (remaining / period) * period
            // (skips through multiple whole periods until it's less than a period away from the limit)

            repeat(remaining % period) {
                grid.spinCycle()
                cycle++
            }
        } ?: run {
            previouslySeenHashes[hash] = cycle
        }
    }
}

println(
    grid
        .also { it.spinUntil(1_000_000_000) }
        .calculateLoad()
)
