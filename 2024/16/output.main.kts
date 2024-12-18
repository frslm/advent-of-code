import java.io.File
import kotlin.math.abs

val maze = File("input.txt").readLines().map { line -> line.map { it } }

typealias Vector = Pair<Int, Int>
operator fun Vector.plus(vector: Vector): Vector = first + vector.first to second + vector.second
operator fun Vector.minus(vector: Vector): Vector = first - vector.first to second - vector.second
operator fun <T> List<List<T>>.get(vector: Vector): T = this[vector.first][vector.second]

enum class Direction { N, S, W, E }

data class Orientation(
    val location: Vector,
    val direction: Direction
) {
    val stepInDirection get(): Orientation =
        copy(
            location = location + when (direction) {
                Direction.N -> -1 to 0
                Direction.S -> 1 to 0
                Direction.W -> 0 to -1
                Direction.E -> 0 to 1
            }
        )

    val stepBackInDirection get(): Orientation =
        copy(
            location = location - when (direction) {
                Direction.N -> -1 to 0
                Direction.S -> 1 to 0
                Direction.W -> 0 to -1
                Direction.E -> 0 to 1
            }
        )

    val possibleTurns get(): List<Orientation> =
        when (direction) {
            Direction.N, Direction.S -> listOf(Direction.W, Direction.E)
            Direction.W, Direction.E -> listOf(Direction.N, Direction.S)
        }.map { copy(direction = it) }
}

// Part 1:
typealias Score = Long

data class BestScore(var score: Score? = null)

/** Works on examples, but on not the larger maze. */
fun List<List<Char>>.lowestScoreFrom(
    orientation: Orientation,
    scoreSoFar: Score = 0,
    bestScore: BestScore = BestScore(),
    history: Set<Orientation> = emptySet()
): Score? {
    if (orientation in history) { // (kill loops)
        return null
    }

    bestScore.score?.let { // (kill inefficient runs)
        if (scoreSoFar >= it) {
            return null
        }
    }

    if (this[orientation.location] == 'E') {
        return scoreSoFar.also {
            bestScore.score.let { best ->
                if (best == null || it < best) {
                    bestScore.score = it
                }
            }
        }
    } else {
        val newHistory = history + setOf(orientation)

        return (
            listOfNotNull(
                (
                    orientation.location + when (orientation.direction) {
                        Direction.N -> -1 to 0
                        Direction.S -> 1 to 0
                        Direction.W -> 0 to -1
                        Direction.E -> 0 to 1
                    }
                ).let { nextLocation ->
                    if (this[nextLocation] == '#') {
                        null
                    } else {
                        this.lowestScoreFrom(
                            orientation = orientation.copy(location = nextLocation),
                            scoreSoFar = scoreSoFar + 1,
                            bestScore = bestScore,
                            history = newHistory
                        )
                    }
                }
            ) + when (orientation.direction) {
                Direction.N, Direction.S -> listOf(Direction.W, Direction.E)
                Direction.W, Direction.E -> listOf(Direction.N, Direction.S)
            }.map { newDirection ->
                this.lowestScoreFrom(
                    orientation = orientation.copy(direction = newDirection),
                    scoreSoFar = scoreSoFar + 1000,
                    bestScore = bestScore,
                    history = newHistory
                )
            }
        ).filterNotNull().minOrNull()?.also {
            bestScore.score.let { best ->
                if (best == null || it < best) {
                    bestScore.score = it
                }
            }
        }
    }
}

data class Tile(
    val symbol: Char,
    val bestScorePerDirection: MutableMap<Direction, Score> = mutableMapOf()
)

val start = Orientation(
    location = maze
        .map { it.indexOf('S') }
        .let { cols -> cols.indexOfFirst { it != -1 }.let { it to cols[it] } },
    direction = Direction.E
)

val end = maze
    .map { it.indexOf('E') }
    .let { cols -> cols.indexOfFirst { it != -1 }.let { it to cols[it] } }

var bestScoreSoFar: Score? = null

fun List<List<Tile>>.traverse(
    orientationToScoreSoFar: Pair<Orientation, Score>
): List<Pair<Orientation, Score>> {
    bestScoreSoFar?.let {
        if (orientationToScoreSoFar.second > it) {
            return emptyList()
        }
    }

    this[orientationToScoreSoFar.first.location].bestScorePerDirection[orientationToScoreSoFar.first.direction]?.let { bestScore ->
        if (orientationToScoreSoFar.second > bestScore) {
            return emptyList()
        }
    }

    this[orientationToScoreSoFar.first.location].bestScorePerDirection[orientationToScoreSoFar.first.direction].let { bestScore ->
        if (bestScore == null || orientationToScoreSoFar.second < bestScore) {
            this[orientationToScoreSoFar.first.location].bestScorePerDirection[orientationToScoreSoFar.first.direction] = orientationToScoreSoFar.second
            if (orientationToScoreSoFar.first.location == end) {
                bestScoreSoFar.let {
                    if (it == null || orientationToScoreSoFar.second < it) {
                        bestScoreSoFar = orientationToScoreSoFar.second
                    }
                }
            }
        }
    }

    return orientationToScoreSoFar.first.stepInDirection.let {
        listOfNotNull(
            if (this[it.location].symbol != '#') {
                it to orientationToScoreSoFar.second + 1
            } else {
                null
            }
        )
    } + orientationToScoreSoFar.first.possibleTurns.map {
        it to orientationToScoreSoFar.second + 1000
    }
}

val traversedMaze = maze
    .map { line -> line.map { Tile(it) } }
    .also { it[start.location].bestScorePerDirection[start.direction] = 0 }
    .apply {
        val candidates = mutableListOf(start to 0L)

        var count = 0
        while (candidates.isNotEmpty()) {
            if (count % 10 == 0) { // (heuristic to aim towards the end)
                candidates.sortBy {
                    (it.first.location - end).let {
                        abs(it.first) + abs(it.second)
                    }
                }
            }

            candidates.addAll(0, traverse(candidates.take(1).single().also { candidates.removeAt(0) }))
            count++
        }
    }

val bestScore = traversedMaze[end].bestScorePerDirection.values.minOrNull()

println(
    bestScore
)

// Part 2:

// Starting at the end in the orientations with the best score (there could be multiple), traverse backwards towards
// a tile with orientation 0. Each tile touched should be part of a best path.

println(
    mutableSetOf<Vector>().apply {
        val candidates = traversedMaze[end].bestScorePerDirection.filter { it.value == bestScore }.keys.map {
            Orientation(
                location = end,
                direction = it
            )
        }.toMutableList()

        while(candidates.isNotEmpty()) {
            val current = candidates.take(1).single().also { candidates.removeAt(0) }

            add(current.location)

            current.stepBackInDirection.let { previous ->
                traversedMaze[current.location].bestScorePerDirection[current.direction]?.let { currentScore ->
                    traversedMaze[previous.location].bestScorePerDirection[previous.direction]?.let { previousScore ->
                        if (currentScore == previousScore + 1) {
                            candidates.add(previous)
                        }
                    }
                }
            }

            current.possibleTurns.map { previous ->
                traversedMaze[current.location].bestScorePerDirection[current.direction]?.let { currentScore ->
                    traversedMaze[previous.location].bestScorePerDirection[previous.direction]?.let { previousScore ->
                        if (currentScore == previousScore + 1000) {
                            candidates.add(previous)
                        }
                    }
                }
            }
        }
    }.size
)
