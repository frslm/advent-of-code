package main

import (
	"2025/common"
	"fmt"
)

type grid [][]byte

func buildGrid() grid {
	grid := grid{}
	for _, line := range common.ReadLines("input.txt") {
		grid = append(grid, []byte(line))
	}
	return grid
}

func (grid grid) contains(x, y int) bool {
	return x >= 0 && x < len(grid[0]) && y >= 0 && y < len(grid)
}

func (grid grid) accessible(x, y int) bool {
	obstacles := 0
	for _, coords := range [][2]int{{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}} {
		if grid.contains(x+coords[0], y+coords[1]) && grid[y+coords[1]][x+coords[0]] == '@' {
			obstacles++
		}
	}
	return obstacles < 4
}

func part1() {
	grid := buildGrid()
	accessibleLocations := 0

	for y, row := range grid {
		for x, location := range row {
			if location == '@' && grid.accessible(x, y) {
				accessibleLocations++
			}
		}
	}

	fmt.Println(accessibleLocations)
}

func part2() {
	grid := buildGrid()
	removedTotal := 0

	for {
		accessibleLocations := 0

		for y, row := range grid {
			for x, location := range row {
				if location == '@' && grid.accessible(x, y) {
					accessibleLocations++
					grid[y][x] = 'x'
				}
			}
		}

		if accessibleLocations > 0 {
			removedTotal += accessibleLocations
		} else {
			break
		}
	}

	fmt.Println(removedTotal)
}

func main() {
	part1()
	part2()
}
