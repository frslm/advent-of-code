package main

import (
	"2025/common"
	"fmt"
	"math"
	"strconv"
	"strings"
)

type position [2]int64
type tiles []position

func getTiles() tiles {
	tiles := tiles{}
	for _, line := range common.ReadLines("input.txt") {
		tileAsString := strings.Split(line, ",")
		x, _ := strconv.ParseInt(tileAsString[0], 10, 64)
		y, _ := strconv.ParseInt(tileAsString[1], 10, 64)
		tiles = append(tiles, position{x, y})
	}
	return tiles
}

func calculateArea(first, second position) int64 {
	return int64(math.Abs(float64(second[0]-first[0]))+1) *
		int64(math.Abs(float64(second[1]-first[1]))+1)
}

type rectangle struct {
	min, max position
}

func (tiles tiles) intersects(rectangle rectangle) bool {
	for i := range tiles {
		// Set up a 1-dimensional edge of tiles:
		first, second := tiles[i], tiles[(i+1)%len(tiles)]

		// If part of this edge is within the rectangle...
		if (first[0] > rectangle.min[0] || second[0] > rectangle.min[0]) &&
			(first[0] < rectangle.max[0] || second[0] < rectangle.max[0]) &&
			(first[1] > rectangle.min[1] || second[1] > rectangle.min[1]) &&
			(first[1] < rectangle.max[1] || second[1] < rectangle.max[1]) {
			// ...then there's an intersection:
			return true
		}
	}

	return false
}

func part1() {
	tiles := getTiles()

	largestArea := int64(0)
	for i, first := range tiles {
		for _, second := range tiles[i+1:] {
			if area := calculateArea(first, second); area > largestArea {
				largestArea = area
			}
		}
	}

	fmt.Println(largestArea)
}

func part2() {
	tiles := getTiles()

	largestArea := int64(0)
	for i, first := range tiles {
		for _, second := range tiles[i+1:] {
			if area := calculateArea(first, second); area > largestArea && !tiles.intersects(
				rectangle{
					position{min(first[0], second[0]), min(first[1], second[1])},
					position{max(first[0], second[0]), max(first[1], second[1])},
				},
			) {
				largestArea = area
			}

			// Note: This solution will fail if a larger area can be formed completely outside
			//       the set of tiles (since no inside/outside checks are done, only whether
			//       or not any part of the perimeter intersects the rectangle's interior).
		}
	}

	fmt.Println(largestArea)
}

func main() {
	part1()
	part2()
}
