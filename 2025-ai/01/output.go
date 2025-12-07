package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

func part1() int {
	// Open the input file
	file, err := os.Open("input.txt")
	if err != nil {
		fmt.Printf("Error opening file: %v\n", err)
		return 0
	}
	defer file.Close()

	// Start with dial at 50
	position := 50
	count := 0

	// Read file line by line
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" {
			continue
		}

		// Parse the rotation instruction
		direction := line[0]
		distance, err := strconv.Atoi(line[1:])
		if err != nil {
			fmt.Printf("Error parsing distance: %v\n", err)
			continue
		}

		// Apply rotation
		if direction == 'R' {
			// Rotate right (toward higher numbers)
			position = (position + distance) % 100
		} else if direction == 'L' {
			// Rotate left (toward lower numbers)
			// Handle negative modulo correctly
			position = (position - distance + 100) % 100
		}

		// Check if dial ends at 0
		if position == 0 {
			count++
		}
	}

	if err := scanner.Err(); err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}

	return count
}

func part2() int {
	// Open the input file
	file, err := os.Open("input.txt")
	if err != nil {
		fmt.Printf("Error opening file: %v\n", err)
		return 0
	}
	defer file.Close()

	// Start with dial at 50
	position := 50
	count := 0

	// Read file line by line
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" {
			continue
		}

		// Parse the rotation instruction
		direction := line[0]
		distance, err := strconv.Atoi(line[1:])
		if err != nil {
			fmt.Printf("Error parsing distance: %v\n", err)
			continue
		}

		// Count how many times we pass through 0 during rotation
		// We need to count every click where the dial is at 0, excluding the starting position
		// The loop counts clicks 1 through distance, which includes the ending position
		if direction == 'R' {
			// Rotate right: we visit positions pos, pos+1, ..., pos+distance (all mod 100)
			// Count how many times we're at 0 during the rotation (clicks 1 through distance)
			timesAtZero := 0
			for click := 1; click <= distance; click++ {
				pos := (position + click) % 100
				if pos == 0 {
					timesAtZero++
				}
			}
			count += timesAtZero

			// Apply rotation
			position = (position + distance) % 100
		} else if direction == 'L' {
			// Rotate left: we visit positions pos, pos-1, ..., pos-distance (all mod 100)
			// Count how many times we're at 0 during the rotation (clicks 1 through distance)
			timesAtZero := 0
			for click := 1; click <= distance; click++ {
				pos := (position - click + 100) % 100
				if pos == 0 {
					timesAtZero++
				}
			}
			count += timesAtZero

			// Apply rotation
			position = (position - distance + 100) % 100
		}
	}

	if err := scanner.Err(); err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}

	return count
}

func main() {
	result1 := part1()
	result2 := part2()
	fmt.Printf("Part 1: %d\n", result1)
	fmt.Printf("Part 2: %d\n", result2)
}

