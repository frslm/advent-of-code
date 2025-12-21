package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

type Range struct {
	Start int64
	End   int64
}

func part1() int {
	// Read input file
	file, err := os.Open("input.txt")
	if err != nil {
		fmt.Printf("Error opening file: %v\n", err)
		return 0
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	
	// Parse ranges (until blank line)
	var ranges []Range
	inRanges := true
	
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		
		// Check for blank line (separator)
		if line == "" {
			inRanges = false
			continue
		}
		
		if inRanges {
			// Parse range (format: "start-end")
			parts := strings.Split(line, "-")
			if len(parts) != 2 {
				continue
			}
			
			start, err1 := strconv.ParseInt(parts[0], 10, 64)
			end, err2 := strconv.ParseInt(parts[1], 10, 64)
			if err1 != nil || err2 != nil {
				continue
			}
			
			ranges = append(ranges, Range{Start: start, End: end})
		}
	}
	
	// Now process ingredient IDs
	// Reset scanner to read from beginning
	file.Seek(0, 0)
	scanner = bufio.NewScanner(file)
	
	inRanges = true
	freshCount := 0
	
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		
		if line == "" {
			inRanges = false
			continue
		}
		
		if !inRanges {
			// Parse ingredient ID
			id, err := strconv.ParseInt(line, 10, 64)
			if err != nil {
				continue
			}
			
			// Check if ID falls into any range
			isFresh := false
			for _, r := range ranges {
				if id >= r.Start && id <= r.End {
					isFresh = true
					break
				}
			}
			
			if isFresh {
				freshCount++
			}
		}
	}
	
	if err := scanner.Err(); err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	return freshCount
}

func main() {
	result1 := part1()
	fmt.Printf("Part 1: %d\n", result1)
}

