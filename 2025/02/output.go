package main

import (
	"2025/common"
	"fmt"
	"math"
	"strconv"
	"strings"
)

func powerOfTen(digits int) int64 {
	return int64(math.Pow(10, float64(digits)))
}

func calculatePatternSize(value string) int {
	return (len([]rune(value)) + 1) / 2
}

func part1() {
	var sum int64 = 0

	for _, interval := range strings.Split(common.ReadLines("input.txt")[0], ",") {
		limits := strings.Split(interval, "-")

		firstAsString := limits[0]
		lastAsString := limits[1]

		firstAsInt, _ := strconv.ParseInt(firstAsString, 10, 64)
		lastAsInt, _ := strconv.ParseInt(lastAsString, 10, 64)

		for size := calculatePatternSize(firstAsString); size <= calculatePatternSize(lastAsString); size++ {
			multiplier := powerOfTen(size) + 1

			start := max(firstAsInt/multiplier+min(firstAsInt%multiplier, 1), powerOfTen(size-1))
			end := min(lastAsInt/multiplier, powerOfTen(size)-1)

			for pattern := start; pattern <= end; pattern++ {
				sum += pattern * multiplier
			}
		}
	}

	fmt.Println(sum)
}

func reverse(value string) string {
	runes := []rune(value)
	for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
		runes[i], runes[j] = runes[j], runes[i]
	}
	return string(runes)
}

func buildMultiplier(size int, repetitions int) int64 {
	multiplier, _ := strconv.ParseInt(reverse(strings.Repeat(strconv.FormatInt(powerOfTen(size-1), 10), repetitions)), 10, 64)
	return multiplier
}

func getFactorPairs(value int) [][2]int {
	var factorPairs [][2]int

	for i := 1; i <= value; i++ {
		if value%i == 0 {
			factorPairs = append(factorPairs, [2]int{i, value / i})
		}
	}

	return factorPairs
}

func part2() {
	var sum int64 = 0

	for _, interval := range strings.Split(common.ReadLines("input.txt")[0], ",") {
		limits := strings.Split(interval, "-")

		firstAsString := limits[0]
		lastAsString := limits[1]

		firstAsInt, _ := strconv.ParseInt(firstAsString, 10, 64)
		lastAsInt, _ := strconv.ParseInt(lastAsString, 10, 64)

		invalids := map[int64]bool{}

		for digits := len([]rune(firstAsString)); digits <= len([]rune(lastAsString)); digits++ {
			for _, factorPair := range getFactorPairs(digits) {
				size := factorPair[0]
				repetitions := factorPair[1]

				if repetitions > 1 {
					multiplier := buildMultiplier(size, repetitions)

					start := max(firstAsInt/multiplier+min(firstAsInt%multiplier, 1), powerOfTen(size-1))
					end := min(lastAsInt/multiplier, powerOfTen(size)-1)

					for seed := start; seed <= end; seed++ {
						invalids[seed*multiplier] = true
					}
				}
			}
		}

		for invalid := range invalids {
			sum += invalid
		}
	}

	fmt.Println(sum)
}

func main() {
	part1()
	part2()
}
