package main

import (
	"2025/common"
	"fmt"
	"sort"
	"strconv"
	"strings"
)

type position [3]int64

func distanceSquared(first, second position) int64 {
	return (second[0]-first[0])*(second[0]-first[0]) +
		(second[1]-first[1])*(second[1]-first[1]) +
		(second[2]-first[2])*(second[2]-first[2])
}

func readPositions() []position {
	positions := []position{}
	for _, line := range common.ReadLines("input.txt") {
		coords := position{}
		for i, string := range strings.Split(line, ",") {
			coord, _ := strconv.ParseInt(string, 10, 64)
			coords[i] = coord
		}
		positions = append(positions, coords)
	}
	return positions
}

type distance struct {
	value int64
	pair  [2]position
}

func calculatePairDistances(positions []position) []distance {
	distances := []distance{}
	for i, first := range positions {
		for _, second := range positions[i+1:] {
			distances = append(distances, distance{distanceSquared(first, second), [2]position{first, second}})
		}
	}
	return distances
}

type circuit []position
type perPositionCircuit map[position]*circuit

func initializeCircuits(positions []position) perPositionCircuit {
	perPositionCircuit := perPositionCircuit{}
	for _, value := range positions {
		perPositionCircuit[value] = &circuit{value}
	}
	return perPositionCircuit
}

func (perPositionCircuit perPositionCircuit) connectClosest(n int, distances []distance) {
	sort.Slice(distances, func(i, j int) bool { return distances[i].value < distances[j].value })

	connections := 0
	for _, distance := range distances {
		firstCircuit := perPositionCircuit[distance.pair[0]]
		secondCircuit := perPositionCircuit[distance.pair[1]]

		if firstCircuit != secondCircuit {
			merged := append(*firstCircuit, *secondCircuit...)
			for _, position := range merged {
				perPositionCircuit[position] = &merged
			}
		}

		connections++
		if connections == n {
			break
		}
	}
}

func (perPositionCircuit perPositionCircuit) connectAllReturningLastPair(distances []distance) [2]position {
	sort.Slice(distances, func(i, j int) bool { return distances[i].value < distances[j].value })

	for _, distance := range distances {
		firstCircuit := perPositionCircuit[distance.pair[0]]
		secondCircuit := perPositionCircuit[distance.pair[1]]

		if firstCircuit != secondCircuit {
			merged := append(*firstCircuit, *secondCircuit...)
			for _, position := range merged {
				perPositionCircuit[position] = &merged
			}
			if len(merged) == len(perPositionCircuit) {
				return distance.pair
			}
		}
	}

	panic("Somehow failed to connect everything into one circuit!")
}

type circuits []circuit

func (perPositionCircuit perPositionCircuit) getUniqueCircuits() circuits {
	uniqueCircuits := map[*circuit]bool{}
	for _, circuit := range perPositionCircuit {
		uniqueCircuits[circuit] = true
	}

	circuits := circuits{}
	for circuit := range uniqueCircuits {
		circuits = append(circuits, *circuit)
	}
	return circuits
}

func (circuits circuits) sortByLengthDescending() {
	sort.Slice(circuits, func(i, j int) bool { return len(circuits[i]) > len(circuits[j]) })
}

func part1() {
	positions := readPositions()
	distances := calculatePairDistances(positions)
	perPositionCircuit := initializeCircuits(positions)
	perPositionCircuit.connectClosest(1000, distances)
	circuits := perPositionCircuit.getUniqueCircuits()
	circuits.sortByLengthDescending()

	size := int64(1)
	for _, circuit := range circuits[:3] {
		size *= int64(len(circuit))
	}
	fmt.Println(size)
}

func part2() {
	positions := readPositions()
	distances := calculatePairDistances(positions)
	perPositionCircuit := initializeCircuits(positions)
	pair := perPositionCircuit.connectAllReturningLastPair(distances)

	fmt.Println(pair[0][0] * pair[1][0])
}

func main() {
	part1()
	part2()
}
