import java.io.File

val lines = File("input.txt").readLines()

data class Mapping(val destination: Long, val source: Long, val range: Long)

fun List<String>.extractMappings(header: String, endsInDelimiter: Boolean = true): List<Mapping> =
    (this.indexOf(header) + 1)
        .let { this.subList(it, if (endsInDelimiter) this.subList(it, this.size).indexOf("") + it else this.size) }
        .map { line -> line.split(" ").map { it.toLong() }.let { Mapping(destination = it[0], source = it[1], range = it[2]) } }
        .sortedBy { it.source }

val seeds = Regex("seeds: (.*)").matchEntire(lines.first())!!.groups[1]!!.value.split(" ").map { it.toLong() }
val seedToSoil = lines.extractMappings("seed-to-soil map:")
val soilToFertilizer = lines.extractMappings("soil-to-fertilizer map:")
val fertilizerToWater = lines.extractMappings("fertilizer-to-water map:")
val waterToLight = lines.extractMappings("water-to-light map:")
val lightToTemperature = lines.extractMappings("light-to-temperature map:")
val temperatureToHumidity = lines.extractMappings("temperature-to-humidity map:")
val humidityToLocation = lines.extractMappings("humidity-to-location map:", endsInDelimiter = false)

fun List<Mapping>.find(source: Long) =
    this.binarySearch {
        when {
            source < it.source -> 1
            source < it.source + it.range -> 0
            else -> -1
        }
    }

// Part 1:

// Use a binary search to find the correct source range, then use the corresponding mapping to find the destination.

fun Long.findIn(mappings: List<Mapping>): Long =
    mappings.find(this).let { index ->
        if (index >= 0) {
            mappings[index].let { this - it.source + it.destination }
        } else {
            this
        }
    }

println(
    seeds.minOfOrNull {
        it
            .findIn(seedToSoil)
            .findIn(soilToFertilizer)
            .findIn(fertilizerToWater)
            .findIn(waterToLight)
            .findIn(lightToTemperature)
            .findIn(temperatureToHumidity)
            .findIn(humidityToLocation)
    }
)

// Part 2:

// Now that the seed inputs come in very wide ranges, the same approach (of checking one seed at a time) would be too
// slow. So we should instead consider an entire seed range at once; if part of the seed range belongs to a separate
// source-destination mapping bucket, then we split the range and handle each portion separately.

fun List<Pair<Long, Long>>.findIn(mappings: List<Mapping>): List<Pair<Long, Long>> {
    val sources = this.toMutableList()
    val destinations = mutableListOf<Pair<Long, Long>>()

    while(sources.isNotEmpty()) {
        val (source, range) = sources.removeAt(0)

        mappings.find(source).let { index ->
            if (index >= 0) { // (part of or entire source range has a mapping)
                val mapping = mappings[index]
                val rangeBeyondMapping = (source + range) - (mapping.source + mapping.range)
                when {
                    rangeBeyondMapping < 0 -> // (mapping fully contains the source range)
                        destinations.add(source - mapping.source + mapping.destination to range)
                    else -> { // (portion of source range goes beyond the mapping; add it back into the list of sources)
                        destinations.add(source - mapping.source + mapping.destination to range - rangeBeyondMapping)
                        sources.add(source + range - rangeBeyondMapping to rangeBeyondMapping)
                    }
                }
            } else { // (part of or entire source range has no mapping)
                when (val insertionPoint = -index - 1) { // (as defined by the find algorithm)
                    mappings.size -> // (larger than any mapping range, so entire range has no mapping)
                        destinations.add(source to range)
                    else -> { // (portion of source range enters the next mapping; add it back into the list of sources)
                        val rangeIntoNextMapping = (source + range) - mappings[insertionPoint].source

                        destinations.add(source to range - rangeIntoNextMapping)
                        sources.add(source + range - rangeIntoNextMapping to rangeIntoNextMapping)
                    }
                }
            }
        }
    }

    return destinations
}

println(
    seeds.chunked(2).map { it.first() to it.last() }.flatMap {
        listOf(it)
            .findIn(seedToSoil)
            .findIn(soilToFertilizer)
            .findIn(fertilizerToWater)
            .findIn(waterToLight)
            .findIn(lightToTemperature)
            .findIn(temperatureToHumidity)
            .findIn(humidityToLocation)
            .map { (seed, _) -> seed }
    }.min()
)
