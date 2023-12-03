import java.io.File
import java.util.Objects.hash

class Position(val x: Int, val y: Int, val z: Int) {
    override fun equals(other: Any?): Boolean = other is Position && x == other.x && y == other.y && z == other.z
    override fun hashCode(): Int = hash(x, y, z)
}

class Cube(
    var left: Cube? = null, // (x - 1)
    var right: Cube? = null, // (x + 1)
    var up: Cube? = null, // (z + 1)
    var down: Cube? = null, // (z - 1)
    var in_: Cube? = null, // (y + 1)
    var out: Cube? = null // (y - 1)
)

enum class Material { Lava, AirExternal, AirInternal }
typealias Grid = MutableList<MutableList<MutableList<Material?>>>

val size = 20

fun Grid.traverseVolume(position: Triple<Int, Int, Int>, visited: MutableSet<Triple<Int, Int, Int>>): Boolean {
    visited.add(Triple(position.first, position.second, position.third))

    val material = this[position.first][position.second][position.third]

    var touchedBound = false

    listOf(
        Triple(position.first - 1, position.second, position.third) to { position.first == 0 },
        Triple(position.first + 1, position.second, position.third) to { position.first == this@Output_main.size - 1 },
        Triple(position.first, position.second - 1, position.third) to { position.second == 0 },
        Triple(position.first, position.second + 1, position.third) to { position.second == this@Output_main.size - 1 },
        Triple(position.first, position.second, position.third - 1) to { position.third == 0 },
        Triple(position.first, position.second, position.third + 1) to { position.third == this@Output_main.size - 1 }
    ).forEach { (neighbour, atBound) ->
        touchedBound = touchedBound || when {
            atBound() -> true
            neighbour in visited -> false
            this[neighbour.first][neighbour.second][neighbour.third] != material -> false
            else -> this.traverseVolume(neighbour, visited)
        }
    }

    return touchedBound
}

// Part 1:

// Immediately think of a graph network; each cube has six connection points, so for a given xyz coordinate that
// represents a cube, I need to check if a cube at each of the six connection points exists in the input, and if so,
// then I'd bidirectionally link both cubes. Once all cubes are linked, just traverse through each cube and count
// the total number of free faces.

// Convert input to a free-floating graph of cubes:
val graph = File("input.txt").readLines().associate { line ->
    Pair(line.split(',').map { it.toInt() }.let { Position(it[0], it[1], it[2]) }, Cube())
}

// Bidirectionally connect neighbouring cubes:
for ((position, cube) in graph) {
    graph[Position(position.x - 1, position.y, position.z)]?.let { cube.left = it; it.right = cube }
    graph[Position(position.x + 1, position.y, position.z)]?.let { cube.right = it; it.left = cube }

    graph[Position(position.x, position.y, position.z + 1)]?.let { cube.up = it; it.down = cube }
    graph[Position(position.x, position.y, position.z - 1)]?.let { cube.down = it; it.up = cube }

    graph[Position(position.x, position.y + 1, position.z)]?.let { cube.in_ = it; it.out = cube }
    graph[Position(position.x, position.y - 1, position.z)]?.let { cube.out = it; it.in_ = cube }
}

// Count the total number of unconnected cube faces:
println(
    graph.values.sumOf { listOf(it.left, it.right, it.up, it.down, it.in_, it.out).count { face -> face == null } }
)

// Part 2:

// Now how to avoid counting the surface of internal pockets? One approach is to first draw a 3D bounding box
// around the connected cubes, then to insert air cubes in all the spaces. Then traverse through each pocket of air
// cubes; once each air cube is visited (added to a set), then mark the air "External" if any part of it touched the
// bounding box, otherwise mark the air "Internal". Then calculate the surface area of all lava cells that are only
// exposed to external air.

val grid: Grid = MutableList(size) { MutableList(size) { MutableList(size) { null } } }

// Build grid of lava cells:
File("input.txt").forEachLine { line ->
    with(line.split(',').map { it.toInt() }) { grid[this[0]][this[1]][this[2]] = Material.Lava }
}

// Designate air spaces (currently null) as internal or external:
grid.forEachIndexed { x, plane ->
    plane.forEachIndexed { y, line ->
        line.forEachIndexed { z, material ->
            if (material == null) {
                with(mutableSetOf<Triple<Int, Int, Int>>()) {
                    val touchedBound = grid.traverseVolume(Triple(x, y, z), this)

                    this.forEach {
                        grid[it.first][it.second][it.third] =
                            if (touchedBound) Material.AirExternal else Material.AirInternal
                    }
                }
            }
        }
    }
}

// Count the total number of faces exposed to external air:
println(grid.flatMapIndexed { x, plane ->
    plane.flatMapIndexed { y, line ->
        line.mapIndexed { z, material ->
            if (material == Material.Lava) {
                listOf(
                    x == 0 || grid[x - 1][y][z] == Material.AirExternal,
                    x == size - 1 || grid[x + 1][y][z] == Material.AirExternal,

                    y == 0 || grid[x][y - 1][z] == Material.AirExternal,
                    y == size - 1 || grid[x][y + 1][z] == Material.AirExternal,

                    z == 0 || grid[x][y][z - 1] == Material.AirExternal,
                    z == size - 1 || grid[x][y][z + 1] == Material.AirExternal,
                ).count { it }
            } else {
                0
            }
        }
    }
}.sum()) // TODO: For some reason this is too low?
