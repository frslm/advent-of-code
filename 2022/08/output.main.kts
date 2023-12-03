import java.io.File
import java.lang.Integer.max

// Convert the input to a 2D grid of tree heights:
val treeHeights = File("input.txt").readLines().map { line -> line.map { it.digitToInt() } }

// Part 1:

// Idea is to build a 2D grid layer for each cardinal direction, which stores the highest tree seen so far from a
// given cardinal direction. By preprocessing in this manner, we use some memory to avoid the brute force solution
// of repeatedly checking the same line of sights for every tree.
//
// After the 2D grid layers are built, can simply go through each tree and check if any of the four cardinal
// directions has a clear line of sight; if so, then the tree is visible from outside the grid.

// Note: -1 below represents no tree; shorter than the shortest possible tree, 0.

// From north:
val northerly = mutableListOf<List<Int>>()
treeHeights.forEachIndexed { y, row ->
    northerly += if (northerly.isEmpty()) {
        List(row.size) { -1 }
    } else {
        List(row.size) { x -> max(northerly.last()[x], treeHeights[y - 1][x]) }
    }
}

// From south:
val southerly = mutableListOf<List<Int>>()
for (y in (treeHeights.size - 1 downTo 0)) {
    southerly.add(0,
        if (southerly.isEmpty()) {
            List(treeHeights[y].size) { -1 }
        } else {
            List(treeHeights[y].size) { x -> max(southerly.first()[x], treeHeights[y + 1][x]) }
        }
    )
}

// From west:
val westerly = mutableListOf<MutableList<Int>>()
treeHeights.first().forEachIndexed { x, _ ->
    if (westerly.isEmpty()) {
        treeHeights.forEach { _ -> westerly += MutableList(1) { -1 } }
    } else {
        treeHeights.forEachIndexed { y, row -> westerly[y] += max(westerly[y].last(), row[x - 1]) }
    }
}

// From east:
val easterly = mutableListOf<MutableList<Int>>()
for (x in (treeHeights.first().size - 1 downTo 0)) {
    if (easterly.isEmpty()) {
        treeHeights.forEach { _ -> easterly += MutableList(1) { -1 } }
    } else {
        treeHeights.forEachIndexed { y, row -> easterly[y].add(0, max(easterly[y].first(), row[x + 1])) }
    }
}

// Count number of trees that can be seen from the outside of the grid:
println(
    treeHeights.mapIndexed { y, row ->
        row.filterIndexed { x, height ->
            height > northerly[y][x] || height > southerly[y][x] || height > westerly[y][x] || height > easterly[y][x]
        }.size
    }.sum()
)

// Part 2:

// This is kinda the reverse situation now, looking from the inside out. Doesn't seem immediately clear how to do
// this efficiently, so can just brute force it.

println(
    treeHeights.mapIndexed { y, row ->
        row.mapIndexed { x, height ->
            var north = 0
            for (i in y - 1 downTo 0) {
                north += 1
                if (height <= treeHeights[i][x]) break
            }

            var south = 0
            for (i in y + 1 until treeHeights.size) {
                south += 1
                if (height <= treeHeights[i][x]) break
            }

            var west = 0
            for (i in x - 1 downTo 0) {
                west += 1
                if (height <= treeHeights[y][i]) break
            }

            var east = 0
            for (i in x + 1 until treeHeights.first().size) {
                east += 1
                if (height <= treeHeights[y][i]) break
            }

            north * south * west * east
        }.max()
    }.max()
)
