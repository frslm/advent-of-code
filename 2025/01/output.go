package main

import (
	"2025/common"
	"fmt"
	"strconv"
)

func part1() {
	dial, hit_zero := 50, 0

	for _, line := range common.ReadLines("input.txt") {
		var direction int
		switch []rune(line)[0] {
		case 'L':
			direction = -1
		case 'R':
			direction = 1
		}

		distance, _ := strconv.Atoi(line[1:])
		dial = (dial + direction*distance) % 100

		if dial == 0 {
			hit_zero++
		}
	}

	fmt.Println(hit_zero)
}

func part2() {
	dial, hit_zero := 50, 0

	for _, line := range common.ReadLines("input.txt") {
		movement, _ := strconv.Atoi(line[1:])

		switch []rune(line)[0] {
		case 'L':
			for step := 0; step < movement; step++ {
				if dial == 0 {
					dial = 99
				} else if dial > 0 {
					dial--
					if dial == 0 {
						hit_zero++
					}
				}
			}

		case 'R':
			for step := 0; step < movement; step++ {
				if dial == 99 {
					dial = 0
					hit_zero++
				} else if dial < 99 {
					dial++
				}
			}
		}
	}

	fmt.Println(hit_zero)
}

func main() {
	part1()
	part2()
}
