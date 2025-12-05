package main

import (
	"2025/common"
	"fmt"
	"strconv"
	"strings"
)

type interval [2]int64

type database struct {
	ranges []interval
	ids    []int64
}

func buildDatabase() database {
	database := database{}
	parsingRanges := true

	for _, line := range common.ReadLines("input.txt") {
		if parsingRanges {
			if line != "" {
				interval := interval{}

				for i, value := range strings.Split(line, "-") {
					id, _ := strconv.ParseInt(value, 10, 64)
					interval[i] = id
				}

				database.ranges = append(database.ranges, interval)
			} else {
				parsingRanges = false
			}
		} else {
			id, _ := strconv.ParseInt(line, 10, 64)
			database.ids = append(database.ids, id)
		}
	}

	return database
}

func (interval interval) contains(id int64) bool {
	return id >= interval[0] && id <= interval[1]
}

func part1() {
	fresh := 0
	database := buildDatabase()

	for _, id := range database.ids {
		for _, interval := range database.ranges {
			if interval.contains(id) {
				fresh++
				break
			}
		}
	}

	fmt.Println(fresh)
}

func (interval interval) intersects(other interval) bool {
	return interval.contains(other[0]) ||
		interval.contains(other[1]) ||
		other.contains(interval[0]) ||
		other.contains(interval[1])
}

func (left interval) merge(right interval) interval {
	return interval{
		min(left[0], right[0]),
		max(left[1], right[1]),
	}
}

func part2() {
	ranges := buildDatabase().ranges

	for i := 0; i < len(ranges); {
		anyIntervalMerged := false
		for j := i + 1; j < len(ranges); {
			if ranges[i].intersects(ranges[j]) {
				ranges[i] = ranges[i].merge(ranges[j])
				ranges = append(ranges[:j], ranges[j+1:]...)
				anyIntervalMerged = true
			} else {
				j++
			}
		}

		if !anyIntervalMerged {
			i++
		}
	}

	fresh := int64(0)
	for _, interval := range ranges {
		fresh += interval[1] - interval[0] + 1
	}
	fmt.Println(fresh)
}

func main() {
	part1()
	part2()
}
