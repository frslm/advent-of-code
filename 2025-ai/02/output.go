package main

import (
	"fmt"
	"os"
	"strconv"
	"strings"
)

func isInvalidIDPart1(id int) bool {
	// Convert ID to string to check length and pattern
	idStr := strconv.Itoa(id)
	length := len(idStr)
	
	// Must be even length to be split into two equal halves
	if length%2 != 0 {
		return false
	}
	
	// Split into two halves
	half := length / 2
	firstHalf := idStr[:half]
	secondHalf := idStr[half:]
	
	// Check if both halves are identical
	return firstHalf == secondHalf
}

func isInvalidIDPart2(id int) bool {
	// Convert ID to string to check pattern
	idStr := strconv.Itoa(id)
	length := len(idStr)
	
	// Try different pattern lengths (from 1 to length/2)
	// The pattern must repeat at least twice, so max pattern length is length/2
	for patternLen := 1; patternLen <= length/2; patternLen++ {
		// Check if length is divisible by pattern length
		if length%patternLen != 0 {
			continue
		}
		
		// Get the pattern (first patternLen digits)
		pattern := idStr[:patternLen]
		
		// Check if the entire number is made of this pattern repeated
		valid := true
		for i := patternLen; i < length; i += patternLen {
			segment := idStr[i : i+patternLen]
			if segment != pattern {
				valid = false
				break
			}
		}
		
		if valid {
			return true
		}
	}
	
	return false
}

func part1() int {
	// Read input file
	data, err := os.ReadFile("input.txt")
	if err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	// Parse ranges (comma-separated)
	rangesStr := strings.TrimSpace(string(data))
	rangeParts := strings.Split(rangesStr, ",")
	
	sum := 0
	
	// Process each range
	for _, rangePart := range rangeParts {
		rangePart = strings.TrimSpace(rangePart)
		if rangePart == "" {
			continue
		}
		
		// Parse range (format: "start-end")
		parts := strings.Split(rangePart, "-")
		if len(parts) != 2 {
			continue
		}
		
		start, err1 := strconv.Atoi(parts[0])
		end, err2 := strconv.Atoi(parts[1])
		if err1 != nil || err2 != nil {
			continue
		}
		
		// Check each ID in the range
		for id := start; id <= end; id++ {
			if isInvalidIDPart1(id) {
				sum += id
			}
		}
	}
	
	return sum
}

func part2() int {
	// Read input file
	data, err := os.ReadFile("input.txt")
	if err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	// Parse ranges (comma-separated)
	rangesStr := strings.TrimSpace(string(data))
	rangeParts := strings.Split(rangesStr, ",")
	
	sum := 0
	
	// Process each range
	for _, rangePart := range rangeParts {
		rangePart = strings.TrimSpace(rangePart)
		if rangePart == "" {
			continue
		}
		
		// Parse range (format: "start-end")
		parts := strings.Split(rangePart, "-")
		if len(parts) != 2 {
			continue
		}
		
		start, err1 := strconv.Atoi(parts[0])
		end, err2 := strconv.Atoi(parts[1])
		if err1 != nil || err2 != nil {
			continue
		}
		
		// Check each ID in the range
		for id := start; id <= end; id++ {
			if isInvalidIDPart2(id) {
				sum += id
			}
		}
	}
	
	return sum
}

func main() {
	result1 := part1()
	result2 := part2()
	fmt.Printf("Part 1: %d\n", result1)
	fmt.Printf("Part 2: %d\n", result2)
}

