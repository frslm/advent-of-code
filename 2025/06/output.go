package main

import (
	"2025/common"
	"fmt"
	"strconv"
	"strings"
)

type problem struct {
	values    []int64
	operation string
}

type worksheet []problem

func readWorksheet() worksheet {
	lines := common.ReadLines("input.txt")

	worksheet := worksheet{}
	for _, operator := range strings.Fields(lines[len(lines)-1]) {
		worksheet = append(worksheet, problem{[]int64{}, operator})
	}

	for _, line := range lines[:len(lines)-1] {
		for column, string := range strings.Fields(line) {
			value, _ := strconv.ParseInt(string, 10, 64)
			worksheet[column].values = append(worksheet[column].values, value)
		}
	}

	return worksheet
}

func readWorksheetRightToLeftVertically() worksheet {
	lines := common.ReadLines("input.txt")

	worksheet := worksheet{}
	columnRanges := [][2]int{}

	for column, operator := range lines[len(lines)-1] {
		if operator != ' ' {
			worksheet = append(worksheet, problem{[]int64{}, string(operator)})

			if len(columnRanges)-1 >= 0 {
				columnRanges[len(columnRanges)-1][1] = column - 1
			}

			columnRanges = append(columnRanges, [2]int{column, -1}) // (arbitrary sentinel value)
		}
	}

	columnRanges[len(columnRanges)-1][1] = len(lines[len(lines)-1])

	for i, interval := range columnRanges {
		for column := interval[0]; column < interval[1]; column++ {
			var valueAsString []byte

			for _, line := range lines[:len(lines)-1] {
				if line[column] != ' ' {
					valueAsString = append(valueAsString, line[column])
				}
			}

			value, _ := strconv.ParseInt(string(valueAsString), 10, 64)
			worksheet[i].values = append(worksheet[i].values, value)
		}
	}

	return worksheet
}

type operate func(left, right int64) int64

func reduce(values []int64, operate operate) int64 {
	result := values[0]
	for _, value := range values[1:] {
		result = operate(result, value)
	}
	return result
}

func solveWorksheet(read func() worksheet) int64 {
	grandTotal := int64(0)

	for _, problem := range read() {
		var operate operate
		switch problem.operation {
		case "+":
			operate = func(left, right int64) int64 {
				return left + right
			}
		case "*":
			operate = func(left, right int64) int64 {
				return left * right
			}
		}

		grandTotal += reduce(problem.values, operate)
	}

	return grandTotal
}

func part1() {
	fmt.Println(solveWorksheet(readWorksheet))
}

func part2() {
	fmt.Println(solveWorksheet(readWorksheetRightToLeftVertically))
}

func main() {
	part1()
	part2()
}
