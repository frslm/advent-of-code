import java.io.File

typealias Elevation = Char
typealias Depth = Int

fun Elevation.canReach(elevation: Elevation): Boolean = elevation.code - this.code <= 1

class Node(
    val elevation: Elevation,
    val paths: MutableList<Node> = mutableListOf(),
    var depth: Depth? = null
)

private typealias Grid = List<List<Node>>
private typealias Start = Node
private typealias End = Node

fun File.buildGrid(): Triple<Grid, Start, End> {
    var start: Start? = null
    var end: End? = null

    return Triple(
        this.readLines().map { row ->
            row.map { char ->
                when (char) {
                    'S' -> Node('a').also { start = it }
                    'E' -> Node('z').also { end = it }
                    else -> Node(char)
                }
            }
        },
        start ?: throw Exception("Couldn't find start node!"),
        end ?: throw Exception("Couldn't find end node!")
    )
}

fun Node.traverse(depth: Depth = 0) {
    if (this.depth == null || this.depth!! > depth) {
        this.depth = depth
        this.paths.forEach { it.traverse(depth + 1) }
    }
}

fun Grid.neighboursOf(x: Int, y: Int) =
    listOfNotNull(
        if (x > 0) this[y][x - 1] else null,
        if (x < this.first().size - 1) this[y][x + 1] else null,
        if (y > 0) this[y - 1][x] else null,
        if (y < this.size - 1) this[y + 1][x] else null,
    )

fun File.findShortestPathFromStart(): Depth? {
    // Should build a graph, then traverse it recursively until all paths from S to E are found, making sure not to
    // revisit already-visited nodes. Of all the paths found, the shortest number of steps needed should be returned.

    // To build it, could first create a 2D grid of unconnected nodes, then traverse the grid to connect each node to
    // its neighbours, if possible.

    // Then can traverse the graph by looking at all of a node's connected nodes and passing through a set of visited
    // nodes. When backtracking, can pop from this set. When reaching the final destination, can just return the size of
    // this set minus one (number of steps _between_ a bunch of nodes is the number of nodes minus one).

    // Edit: After running the implementation, it never completed on a bigger grid, likely because it's trying out every
    // single path, which is too big of a search space. Thought about marking certain nodes as "never visit again", but
    // that's path dependent, so I'd be blocking off nodes that might actually be valid in some other paths. Will
    // instead store the distance from the start for each node, and avoid visiting a node if my current distance exceeds
    // the stored distance. This is also the basic approach Dijkstra's path-finding algorithm uses.

    // Build unconnected grid:
    val (grid, start, end) = this.buildGrid()

    // Connect traversable neighbours:
    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, node ->
            grid.neighboursOf(x, y).forEach {
                if (node.elevation.canReach(it.elevation)) node.paths += it
            }
        }
    }

    // Traverse the nodes:
    start.traverse()
    return end.depth
}

fun File.findShortestPathFromAnyStart(): Depth? {
    // Of course, this part breaks the above implementation since there are now multiple starting locations, and we
    // need to find the closest start at elevation 'a'. So we can simply reverse the implementation to start at the
    // destination and work backwards to mark the distance to the end, then we simply go through all 'a' tiles and find
    // the closest one to the destination.

    // Build unconnected grid:
    val (grid, _, end) = this.buildGrid()

    // Backwards-connect traversable neighbours:
    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, node ->
            grid.neighboursOf(x, y).forEach {
                if (it.elevation.canReach(node.elevation)) node.paths += it
            }
        }
    }

    // Traverse the nodes:
    end.traverse()
    return grid.minOfOrNull { row -> row.filter { it.elevation == 'a' }.mapNotNull { it.depth }.min() }
}

// Part 1:
println(File("input.txt").findShortestPathFromStart())

// Part 2:
println(File("input.txt").findShortestPathFromAnyStart())
