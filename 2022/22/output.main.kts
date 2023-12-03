import java.io.File

enum class Tile { Open, Wall, Warp }

sealed class Instruction

class Move(val distance: Int) : Instruction()

enum class Direction { L, R }
class Turn(val direction: Direction) : Instruction()

enum class Cardinal { N, E, S, W }

val lines = File("input.txt").readLines()

val grid = lines.subList(0, lines.size - 2).map { line ->
    line.map {
        when (it) {
            ' ' -> Tile.Warp
            '.' -> Tile.Open
            '#' -> Tile.Wall
            else -> throw Exception("Unknown tile `$it`!")
        }
    }
}

val instructions = lines.last()
    .replace("L", " L ")
    .replace("R", " R ").trim()
    .split(" ")
    .map { instruction -> instruction.toIntOrNull()?.let { Move(it) } ?: Turn(Direction.valueOf(instruction)) }

// Part 1:

// Regular grid traversal, except with wrapping around the edges.

// Begin at top-left-most open tile:
var x = grid.first().indexOfFirst { it == Tile.Open }
var y = 0
var h = Cardinal.E

// Move according to the instructions:
instructions.forEach { instruction ->
    when (instruction) {
        is Turn -> {
            h = when (instruction.direction) {
                Direction.L -> when (h) {
                    Cardinal.N -> Cardinal.W
                    Cardinal.E -> Cardinal.N
                    Cardinal.S -> Cardinal.E
                    Cardinal.W -> Cardinal.S
                }

                Direction.R -> when (h) {
                    Cardinal.N -> Cardinal.E
                    Cardinal.E -> Cardinal.S
                    Cardinal.S -> Cardinal.W
                    Cardinal.W -> Cardinal.N
                }
            }
        }

        is Move -> {
            val (dx, dy) = when (h) {
                Cardinal.N -> 0 to -1
                Cardinal.E -> +1 to 0
                Cardinal.S -> 0 to +1
                Cardinal.W -> -1 to 0
            }

            for (i in 0 until instruction.distance) {
                when (grid.getOrNull(y + dy)?.getOrNull(x + dx)) {
                    Tile.Open -> {
                        x += dx
                        y += dy
                    }

                    Tile.Wall -> break
                    Tile.Warp, null -> {
                        var teleX = x
                        var teleY = y

                        while (grid.getOrNull(teleY - dy)?.getOrNull(teleX - dx) in setOf(Tile.Open, Tile.Wall)) {
                            teleX -= dx
                            teleY -= dy
                        }

                        when (grid.getOrNull(teleY)?.getOrNull(teleX)) {
                            Tile.Open -> {
                                x = teleX
                                y = teleY
                            }

                            Tile.Wall -> break
                            Tile.Warp, null -> throw Exception("How did we hit warp if we checked against it above?")
                        }
                    }
                }
            }
        }

        else -> throw Exception("Don't know how to handle $instruction!")
    }
}

// Print password:
println(
    1000 * (y + 1) + 4 * (x + 1) + when (h) {
        Cardinal.E -> 0
        Cardinal.S -> 1
        Cardinal.W -> 2
        Cardinal.N -> 3
    }
)

// Part 2:

// Now need to somehow wrap the sides of the cube to figure out warp links... If I can manage to build a function
// that spits out the new x,y,h on the cube given my current x,y,h, then I can just call that instead of my current
// wrapping code; all other code can remain as is.
//
// First, can scan entire 50x50 sections of the grid to pick out the sides; a section is either completely empty or
// completely full. Then for each face, check if there are two adjacent connections, and if so, link them through
// one rotation. Again for each face, check if there are two adjacent connections, with one connection being a
// single rotation link, and if so, link them through two rotations. Finally, for each face, check if there are two
// adjacent connections, with one connection being a double rotation link, and if so, link them through three
// rotations.

println() // TODO
