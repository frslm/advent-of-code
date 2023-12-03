import java.io.File

abstract class Entity(val name: String) {
    abstract fun calculateSize(sizes: MutableMap<Directory, Int>): Int
}

class File(name: String, private val size: Int) : Entity(name) {
    override fun calculateSize(sizes: MutableMap<Directory, Int>) = size
}

class Directory(name: String) : Entity(name) {
    private var parent: Directory? = null
    private val children: MutableList<Entity> = mutableListOf()

    override fun calculateSize(sizes: MutableMap<Directory, Int>) =
        this.children.sumOf { it.calculateSize(sizes) }.also {
            if (this !in sizes) sizes[this] = it
        }

    fun buildTree(lines: List<String>) {
        var directory = this
        var command: String? = null
        lines.map { it.split(" ") }.forEach { fragments ->
            when (fragments[0]) {
                "$" -> {
                    command = fragments[1]
                    when (command) {
                        "cd" -> directory = when (val argument = fragments[2]) {
                            "/" -> this
                            ".." -> directory.parent ?: throw Exception("No parent!")
                            else -> directory.children.first { it.name == argument } as? Directory
                                ?: throw Exception("Can only 'cd' into a directory!")
                        }

                        "ls" -> {} // (nothing else to parse, read the output next)
                        else -> throw Exception("Unknown command!")
                    }
                }

                else -> when (command) {
                    "ls" -> when (fragments[0]) {
                        "dir" -> Directory(fragments[1])
                        else -> File(fragments[1], fragments[0].toInt())
                    }.let { child ->
                        directory.children.add(child)
                        if (child is Directory) child.parent = directory
                    }

                    else -> throw Exception("Don't know how to parse this command's output!")
                }
            }
        }
    }
}

// Build the directory tree:
val root = Directory("").also { it.buildTree(File("input.txt").readLines()) }

// Map each directory to its size:
val sizes = mutableMapOf<Directory, Int>()
val totalSize = root.calculateSize(sizes)

// Part 1:
println(sizes.values.filter { it <= 100000 }.sum())

// Part 2:
println(sizes.values.filter { totalSize - it + 30000000 <= 70000000 }.min())
