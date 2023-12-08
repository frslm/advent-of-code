import java.io.File

class Card(value: Char) {
    private val regularRank = RANKS_REGULAR.indexOf(value)
    private val weakJokerRank = RANKS_WEAK_JOKER.indexOf(value)

    fun compareTo(other: Card, version: Version): Int =
        when (version) {
            Version.Regular -> regularRank.compareTo(other.regularRank)
            Version.WeakJoker -> weakJokerRank.compareTo(other.weakJokerRank)
        }

    enum class Version { Regular, WeakJoker }

    companion object {
        private val RANKS_REGULAR = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')
        private val RANKS_WEAK_JOKER = listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J')
    }
}

class Hand(value: String) {
    private val cards = value.map { Card(it) }

    private fun String.getCopies() = this.groupingBy { it }.eachCount().values.sortedDescending()

    private val regularRank = RANKS.indexOf(value.getCopies())
    private val wildcardJokerRank = RANKS.indexOf(
        value.partition { it == 'J' }.let { (jokers, others) ->
            if (others.isNotEmpty()) {
                others.getCopies().mapIndexed { index, count ->
                    count + if (index == 0) jokers.length else 0
                }
            } else {
                jokers.getCopies()
            }
        }
    )

    fun compareTo(other: Hand, handVersion: Version, cardVersion: Card.Version): Int =
        when (handVersion) {
            Version.Regular -> regularRank.compareTo(other.regularRank)
            Version.WildcardJoker -> wildcardJokerRank.compareTo(other.wildcardJokerRank)
        }.let { handComparison ->
            if (handComparison != 0) {
                handComparison
            } else {
                cards.zip(other.cards).forEach { (leftCard, rightCard) ->
                    leftCard.compareTo(rightCard, cardVersion).also {
                        if (it != 0) return it
                    }
                }.let { 0 }
            }
        }

    enum class Version { Regular, WildcardJoker }

    companion object {
        private val RANKS = listOf(
            listOf(5),
            listOf(4, 1),
            listOf(3, 2),
            listOf(3, 1, 1),
            listOf(2, 2, 1),
            listOf(2, 1, 1, 1),
            listOf(1, 1, 1, 1, 1),
        )
    }
}

fun List<Pair<Hand, Long>>.calculateTotalWinnings(handVersion: Hand.Version, cardVersion: Card.Version) =
    this
        .sortedWith { (left, _), (right, _) -> left.compareTo(right, handVersion, cardVersion) }
        .reversed()
        .mapIndexed { index, (_, bid) -> bid * (index + 1) }
        .sum()

val handsWithBids = File("input.txt").readLines().map { line ->
    line.split(" ").let { (hand, bid) -> Hand(hand) to bid.toLong() }
}

// Part 1:

// Can do a sort in O(NlogN); with only 1000 elements it should compute fairly quickly. Alternatively, can group all
// hands by type, then for each type group, convert each hand into a corresponding base-16 number and sort by that.
// It'll still be O(NlogN), but each sort iteration's comparison should be much quicker (comparing numbers instead of
// re-assessing the value of one hand compared to the other). Will just go for the simpler sort approach for now.

println(handsWithBids.calculateTotalWinnings(Hand.Version.Regular, Card.Version.Regular))

// Part 2:

// Just modify the sorters by providing the correct game versions.

println(handsWithBids.calculateTotalWinnings(Hand.Version.WildcardJoker, Card.Version.WeakJoker))
