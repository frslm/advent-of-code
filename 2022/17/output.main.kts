import java.io.File

enum class Material { Air, Stone }

typealias Rock = List<List<Material>>

class Chamber(
    val rows: MutableList<MutableList<Material>> = mutableListOf(),
    var hiddenHeight: Long = 0L,
    val xOffsets: MutableList<Int> = mutableListOf()
) {
    fun top() = rows.indexOfFirst { Material.Stone in it }

    fun height() = rows.size + hiddenHeight - top()

    fun print() {
        println()
        rows.forEach { row ->
            println(row.map {
                when (it) {
                    Material.Air -> '.'
                    Material.Stone -> '#'
                }
            }.joinToString(""))
        }
    }
}

val airGap = 3
val chamberWidth = 7

val rocks: List<Rock> = listOf(
    listOf(listOf(Material.Stone, Material.Stone, Material.Stone, Material.Stone)),

    listOf(
        listOf(Material.Air, Material.Stone, Material.Air),
        listOf(Material.Stone, Material.Stone, Material.Stone),
        listOf(Material.Air, Material.Stone, Material.Air),
    ),

    listOf(
        listOf(Material.Air, Material.Air, Material.Stone),
        listOf(Material.Air, Material.Air, Material.Stone),
        listOf(Material.Stone, Material.Stone, Material.Stone),
    ),

    listOf(
        listOf(Material.Stone),
        listOf(Material.Stone),
        listOf(Material.Stone),
        listOf(Material.Stone),
    ),

    listOf(
        listOf(Material.Stone, Material.Stone),
        listOf(Material.Stone, Material.Stone),
    )
)

fun Chamber.dropRock(rock: Rock, gasGenerator: Iterator<Char>) {
    // Start above the topmost-rock or floor, two units from the left edge:
    var top = this.top()
    if (top == -1) top = 0

    var rockX = 2
    var rockY = top - airGap - rock.size

    if (rockY < 0) {
        // Add rows of air to the top of the chamber, and enough extra space to fit the rock:
        this.rows.addAll(0, MutableList(-rockY) { MutableList(chamberWidth) { Material.Air } })
        rockY = 0
    }

    // Keep simulating until the rock can't drop any farther:
    while (true) {
        // Shift rock with gas jet:
        val gasShift = when (gasGenerator.next()) {
            '<' -> -1
            '>' -> +1
            else -> throw Exception("Unknown gas burst!")
        }

        if (
            !rock.flatMapIndexed { y, row ->
                row.mapIndexed { x, material ->
                    material == Material.Stone && (rockX + x + gasShift < 0 || rockX + x + gasShift >= this.rows.first().size || this.rows[rockY + y][rockX + x + gasShift] == Material.Stone)
                }
            }.any { it }
        ) {
            rockX += gasShift
        }

        // Drop rock one unit down:
        if (
            !rock.flatMapIndexed { y, row ->
                row.mapIndexed { x, material ->
                    material == Material.Stone && (rockY + y + 1 >= this.rows.size || this.rows[rockY + y + 1][rockX + x] == Material.Stone)
                }
            }.any { it }
        ) {
            rockY += 1
        } else { // (rock can't move down, so it settles in place)
            // Place the rock in the chamber:
            rock.forEachIndexed { y, row ->
                row.forEachIndexed { x, material ->
                    if (material == Material.Stone) this.rows[rockY + y][rockX + x] = Material.Stone
                }
            }

            this.xOffsets += rockX

            // If any of the modified rows is completely made of stone, then delete that row and everything below it to
            // save list space:
            rock.indices.map { rockY + it }.firstOrNull { row -> this.rows[row].all { it == Material.Stone } }
                ?.let {
                    this.rows.subList(it, this.rows.size).let { blockedRows ->
                        this.hiddenHeight += blockedRows.size
                        blockedRows.clear()
                    }
                }

            break
        }
    }
}

listOf(
    // Part 1:
    2022,
    // Immediate approach is to keep everything in memory as a 2D visual grid. We need to keep all data as long as there
    // are gaps, since a rock could keep falling through the gaps arbitrarily far. However, if the rocks completely
    // block a left-to-right contiguous section (even if slanted), then we could actually throw out everything below,
    // but maybe this is premature for now. Start with small grid and just add enough space as needed.

    // Part 2:
    1000000000000L
    // Added optimization to delete all rows below a stone-only row, like in Tetris, so that the chamber doesn't
    // grow forever. This fixes the space complexity issue; it'll now be effectively bounded to a few-hundred height
    // of rocks no matter how many iterations go through. However, time complexity is still unbounded; runtime
    // becomes unmanageable with more rocks. Simply placing a rock costs around 1000 ns, but over a trillion
    // iterations, even that will balloon to 5 hours, and this is the quickest part of the program by a factor of
    // 100.
    //
    // So perhaps we're not meant to simulate this problem on a grid, but find some deeper pattern that we can
    // calculate? Looking at the answer for each power of 10 of rocks shows an interesting pattern, all the heights
    // end up starting with the same digits, which is surprising; I'd have thought there'd be no relation... So if I
    // can figure out this pattern, then perhaps I can skip to the answer using some math shortcut?
    //
    // If I play with the example input of ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>", I notice that after rocks 45,
    // 80, 115, 150, etc. (every 35 rocks), the topmost pattern of rocks repeats for some reason.
    //
    // By grouping every 5 settled rocks together and concatenating their x positions, I can represent a pattern of
    // rocks as a number (e.g. if rock 0 settled 1 unit from the left, rock 1 settled 3 units, 2->0, 3->2, 4->3,
    // then I can concatenate the 5 rocks' positions as 13023). Then I should be able to use a chain of those
    // pattern numbers to figure out where a cycle starts, then I can go from there to skip all cyclic calculations
    // to reach the result I want after any number of rocks, no matter how large.
).forEach { numRocks ->
    val gasGenerator = iterator {
        with(File("input.txt").readLines()) {
            assert(this.size == 1)
            this.first().let { pattern ->
                var i = 0
                while (true) {
                    yield(pattern[i])
                    i = (i + 1) % pattern.length
                }
            }
        }
    }

    with(Chamber()) {
        val patterns = mutableListOf<Long>()
        val patternSightings =
            mutableMapOf<Long, MutableList<Pair<Long, Int>>>()

        var skippedAhead = false

        var i = 0L
        while (i < numRocks) {
            val index = (i % rocks.size.toLong()).toInt()

            // Every time a cycle of rocks completes...
            if (index == 0 && this.xOffsets.size > 0 && !skippedAhead) {
                // ...condense the positions of the last cycle into number, to make pattern comparisons possible:
                val pattern = this.xOffsets.takeLast(rocks.size).joinToString("") { it.toString() }.toLong()
                patterns += pattern

                // ...update the number of times this pattern was seen before:
                val info = patternSightings[pattern] ?: run {
                    mutableListOf<Pair<Long, Int>>().also { patternSightings[pattern] = it }
                }
                info += Pair(this.height(), patterns.size - 1)

                // ...if this pattern was seen at least 3 times before, and the two contained stretches of rock
                // heights and patterns are identical...
                if (info.size >= 3) {
                    val sightings = info.takeLast(3)
                    if (sightings[2].first - sightings[1].first == sightings[1].first - sightings[0].first &&
                        sightings[2].second - sightings[1].second == sightings[1].second - sightings[0].second &&
                        (0 until sightings[2].second - sightings[1].second).map {
                            patterns[sightings[0].second + it] == patterns[sightings[1].second + it]
                        }.all { it }
                    ) {
                        // ...then simulate dropping complete cycles of rocks until we get very close to the
                        // required number of rocks:
                        val rockHeightAddedPerCycle = sightings[1].first - sightings[0].first
                        val rocksAddedPerCycle = (sightings[1].second - sightings[0].second) * rocks.size

                        val fullCyclesNeeded = (numRocks - i) / rocksAddedPerCycle
                        this.hiddenHeight += fullCyclesNeeded * rockHeightAddedPerCycle
                        i += fullCyclesNeeded * rocksAddedPerCycle
                        skippedAhead = true

                        if (i == numRocks) break
                    }
                }
            }

            this.dropRock(rocks[index], gasGenerator)
            i++
        }

        println("Rock height: ${this.height()}, skipped ahead: $skippedAhead")
    }
}
