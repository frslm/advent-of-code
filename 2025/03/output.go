package main

import (
	"2025/common"
	"fmt"
	"math"
)

func part1() {
	totalJoltage := 0
	for _, line := range common.ReadLines("input.txt") {
		var digits []int
		for _, char := range []rune(line) {
			digits = append(digits, int(char-'0'))
		}

		highestJoltage := 0
		for i, left := range digits {
			for _, right := range digits[i+1:] {
				joltage := left*10 + right
				if joltage > highestJoltage {
					highestJoltage = joltage
				}
			}
		}

		totalJoltage += highestJoltage
	}

	fmt.Println(totalJoltage)
}

func powerOfTen(digits int) int64 {
	return int64(math.Pow(10, float64(digits)))
}

func highestJoltage(digits []int, index int, length int, memory map[[2]int]int64) int64 {
	cachedMaxJoltage, cacheExists := memory[[2]int{index, length}]

	if cacheExists {
		return cachedMaxJoltage
	} else {
		maxJoltage := int64(0)
		if length > 0 {
			for i := index; i <= len(digits)-length; i++ {
				joltage := int64(digits[i])*powerOfTen(length-1) + highestJoltage(digits, i+1, length-1, memory)
				if joltage > maxJoltage {
					maxJoltage = joltage
				}
			}
		}

		memory[[2]int{index, length}] = maxJoltage

		return maxJoltage
	}
}

func part2() {
	totalJoltage := int64(0)
	for _, line := range common.ReadLines("input.txt") {
		var digits []int
		for _, char := range []rune(line) {
			digits = append(digits, int(char-'0'))
		}

		memory := map[[2]int]int64{}
		totalJoltage += highestJoltage(digits, 0, 12, memory)
	}

	fmt.Println(totalJoltage)
}

func main() {
	part1()
	part2()
}
