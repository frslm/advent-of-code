import java.io.File

val line = File("input.txt").readText()

fun String.decode(): List<Long?> {
    var id = -1L
    return this
        .map { it.digitToInt() }
        .flatMapIndexed { index, length ->
            if (index % 2 == 0) { // (file)
                id++
                List(length) { id }
            } else { // (free space)
                List(length) { null }
            }
        }
}

fun List<Long?>.checksum(): Long =
    this.mapIndexed { index, value -> value?.let { index * it } ?: 0 }.sum()

fun MutableList<Long?>.move(source: Int, destination: Int) {
    this[destination] = this[source]
    this[source] = null
}

// Part 1:
fun List<Long?>.compactFiles(): List<Long?> =
    this.toMutableList().apply {
        var indexOfFirstFreeSpace = 0
        var indexOfLastFile = size - 1

        while (true) {
            while (indexOfFirstFreeSpace < size && this[indexOfFirstFreeSpace] != null) {
                indexOfFirstFreeSpace++
            }

            while (indexOfLastFile >= 0 && this[indexOfLastFile] == null) {
                indexOfLastFile--
            }

            if (indexOfFirstFreeSpace < indexOfLastFile) {
                this.move(indexOfLastFile, indexOfFirstFreeSpace)
            } else {
                break
            }
        }
    }

println(
    line.decode().compactFiles().checksum()
)

// Part 2:
fun List<Long?>.seekRightFrom(index: Int, check: (Long?) -> Boolean): Pair<Int, Int> {
    var left = index; while (left < size && !check(this[left])) left++
    var right = left; while (right + 1 < size && this[right + 1] == this[left]) right++
    return left to right
}

fun List<Long?>.seekLeftFrom(index: Int, check: (Long?) -> Boolean): Pair<Int, Int> {
    var right = index; while (right >= 0 && !check(this[right])) right--
    var left = right; while (left - 1 >= 0 && this[left - 1] == this[right]) left--
    return left to right
}

val Pair<Int, Int>.length get() = second - first + 1

fun List<Long?>.compactFileBlocks(): List<Long?> =
    this.toMutableList().apply {
        var firstFreeSpace = 0 to 0
        var lastFile = size - 1 to size - 1

        while (lastFile.second >= 0) {
            firstFreeSpace = seekRightFrom(firstFreeSpace.first) { it == null }
            lastFile = seekLeftFrom(lastFile.second) { it != null }

            var freeSpace = firstFreeSpace

            while (freeSpace.first < lastFile.first) {
                if (lastFile.length <= freeSpace.length) {
                    (0 until lastFile.length).forEach {
                        this.move(lastFile.first + it, freeSpace.first + it)
                    }

                    break
                } else {
                    freeSpace = seekRightFrom(freeSpace.second + 1) { it == null }
                }
            }

            lastFile = lastFile.first - 1 to lastFile.first - 1
        }
    }

println(
    line.decode().compactFileBlocks().checksum()
)
