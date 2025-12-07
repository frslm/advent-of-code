package main

import (
	"fmt"
	"os"
	"strconv"
	"strings"
)

func findMaxJoltage(bank string) int {
	// Find the maximum 2-digit number we can form by selecting 2 batteries in order
	maxJoltage := 0
	
	// Try all pairs of positions (i, j) where i < j
	for i := 0; i < len(bank); i++ {
		for j := i + 1; j < len(bank); j++ {
			// Form the 2-digit number from digits at positions i and j
			first := int(bank[i] - '0')
			second := int(bank[j] - '0')
			joltage := first*10 + second
			
			if joltage > maxJoltage {
				maxJoltage = joltage
			}
		}
	}
	
	return maxJoltage
}

func part1() int {
	// Read input file
	data, err := os.ReadFile("input.txt")
	if err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	// Split into lines (each line is a bank)
	lines := strings.Split(strings.TrimSpace(string(data)), "\n")
	
	totalJoltage := 0
	
	// Process each bank
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" {
			continue
		}
		
		maxJoltage := findMaxJoltage(line)
		totalJoltage += maxJoltage
	}
	
	return totalJoltage
}

func findMaxJoltage12(bank string) int64 {
	// Find the maximum 12-digit number we can form by selecting exactly 12 batteries in order
	// Use greedy approach: at each position, pick the maximum digit available
	// while ensuring we can still complete the 12-digit number
	
	bankLen := len(bank)
	needed := 12
	
	if bankLen < needed {
		return 0 // Not enough batteries
	}
	
	// Build the result digit by digit
	result := make([]byte, needed)
	startPos := 0 // Starting position for current digit selection
	
	for pos := 0; pos < needed; pos++ {
		// How many digits we still need after this one
		remaining := needed - pos - 1
		// We can search from startPos to (bankLen - remaining - 1)
		endPos := bankLen - remaining
		
		// Find the maximum digit in the available range
		maxDigit := byte('0')
		maxPos := startPos
		for i := startPos; i < endPos; i++ {
			if bank[i] > maxDigit {
				maxDigit = bank[i]
				maxPos = i
			}
		}
		
		result[pos] = maxDigit
		startPos = maxPos + 1
	}
	
	// Convert to integer
	resultStr := string(result)
	joltage, err := strconv.ParseInt(resultStr, 10, 64)
	if err != nil {
		return 0
	}
	
	return joltage
}

func part2() int64 {
	// Read input file
	data, err := os.ReadFile("input.txt")
	if err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	// Split into lines (each line is a bank)
	lines := strings.Split(strings.TrimSpace(string(data)), "\n")
	
	var totalJoltage int64 = 0
	
	// Process each bank
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" {
			continue
		}
		
		maxJoltage := findMaxJoltage12(line)
		totalJoltage += maxJoltage
	}
	
	return totalJoltage
}

func main() {
	result1 := part1()
	result2 := part2()
	fmt.Printf("Part 1: %d\n", result1)
	fmt.Printf("Part 2: %d\n", result2)
}

