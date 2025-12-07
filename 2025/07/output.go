package main

import (
	"2025/common"
	"fmt"
)

func buildDiagram() [][]byte {
	grid := [][]byte{}
	for _, line := range common.ReadLines("input.txt") {
		grid = append(grid, []byte(line))
	}
	return grid
}

func part1() {
	splits := 0
	diagram := buildDiagram()

	for row := 1; row < len(diagram); row++ {
		for col, char := range diagram[row] {
			if above := diagram[row-1][col]; above == 'S' || above == '|' {
				switch char {
				case '.':
					diagram[row][col] = '|'
				case '^':
					diagram[row][col-1] = '|'
					diagram[row][col+1] = '|'
					splits++
				}
			}
		}
	}

	fmt.Println(splits)
}

func part2() {
	// Instead of marking with '|', mark with the number of individual beams
	// entering a cell, similar to Pascal's triangle, then sum the total number
	// of individual beams at the bottom (marked in hex below):

	// .......S.......
	// .......1.......
	// ......1^1......
	// ......1.1......
	// .....1^2^1.....
	// .....1.2.1.....
	// ....1^3^3^1....
	// ....1.3.3.1....
	// ...1^4^331^1...
	// ...1.4.331.1...
	// ..1^5^434^2^1..
	// ..1.5.434.2.1..
	// .1^154^74.21^1.
	// .1.154.74.21.1.
	// 1^2^a^b^b^211^1
	// 1.2.a.b.b.211.1

	const splitter = -1 // (arbitrary negative sentinel value)

	// First, convert the symbol field to a number field:
	diagram := [][]int{}
	for _, line := range buildDiagram() {
		numbers := []int{}
		for _, char := range line {
			switch char {
			case 'S':
				numbers = append(numbers, 1)
			case '.':
				numbers = append(numbers, 0)
			case '^':
				numbers = append(numbers, splitter)
			}
		}
		diagram = append(diagram, numbers)
	}

	// Second, move the beam through the field:
	for row := 1; row < len(diagram); row++ {
		for col, number := range diagram[row] {
			if above := diagram[row-1][col]; above > 0 {
				switch {
				case number >= 0:
					diagram[row][col] += above
				case number == splitter:
					diagram[row][col-1] += above
					diagram[row][col+1] += above
				}
			}
		}
	}

	// Third, sum the numbers at the bottom:
	splits := 0
	for _, number := range diagram[len(diagram)-1] {
		splits += number
	}
	fmt.Println(splits)
}

func main() {
	part1()
	part2()
}
