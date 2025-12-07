package main

import (
	"fmt"
	"os"
	"strings"
)

func part1() int {
	// Read input file
	data, err := os.ReadFile("input.txt")
	if err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	// Split into lines
	content := strings.TrimSpace(string(data))
	lines := strings.Split(content, "\n")
	
	// Filter out empty lines and trim each line
	var grid []string
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line != "" {
			grid = append(grid, line)
		}
	}
	
	rows := len(grid)
	if rows == 0 {
		return 0
	}
	cols := len(grid[0])
	
	// Count accessible rolls
	accessible := 0
	
	// Check each position
	for i := 0; i < rows; i++ {
		for j := 0; j < cols; j++ {
			// Only check positions with rolls (@)
			if grid[i][j] != '@' {
				continue
			}
			
			// Count adjacent rolls (8 neighbors)
			adjacentCount := 0
			for di := -1; di <= 1; di++ {
				for dj := -1; dj <= 1; dj++ {
					if di == 0 && dj == 0 {
						continue // Skip the current position
					}
					
					ni := i + di
					nj := j + dj
					
					// Check bounds
					if ni >= 0 && ni < rows && nj >= 0 && nj < cols {
						if grid[ni][nj] == '@' {
							adjacentCount++
						}
					}
				}
			}
			
			// Accessible if fewer than 4 adjacent rolls
			if adjacentCount < 4 {
				accessible++
			}
		}
	}
	
	return accessible
}

func part2() int {
	// Read input file
	data, err := os.ReadFile("input.txt")
	if err != nil {
		fmt.Printf("Error reading file: %v\n", err)
		return 0
	}
	
	// Split into lines
	content := strings.TrimSpace(string(data))
	lines := strings.Split(content, "\n")
	
	// Filter out empty lines and trim each line
	var grid []string
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line != "" {
			grid = append(grid, line)
		}
	}
	
	rows := len(grid)
	if rows == 0 {
		return 0
	}
	cols := len(grid[0])
	
	// Convert grid to a mutable 2D slice
	state := make([][]byte, rows)
	for i := 0; i < rows; i++ {
		state[i] = make([]byte, cols)
		copy(state[i], []byte(grid[i]))
	}
	
	totalRemoved := 0
	
	// Keep removing rolls until no more can be removed
	for {
		// Find all accessible rolls in current state
		var toRemove [][2]int
		
		for i := 0; i < rows; i++ {
			for j := 0; j < cols; j++ {
				// Only check positions with rolls (@)
				if state[i][j] != '@' {
					continue
				}
				
				// Count adjacent rolls (8 neighbors)
				adjacentCount := 0
				for di := -1; di <= 1; di++ {
					for dj := -1; dj <= 1; dj++ {
						if di == 0 && dj == 0 {
							continue // Skip the current position
						}
						
						ni := i + di
						nj := j + dj
						
						// Check bounds
						if ni >= 0 && ni < rows && nj >= 0 && nj < cols {
							if state[ni][nj] == '@' {
								adjacentCount++
							}
						}
					}
				}
				
				// Accessible if fewer than 4 adjacent rolls
				if adjacentCount < 4 {
					toRemove = append(toRemove, [2]int{i, j})
				}
			}
		}
		
		// If no rolls to remove, we're done
		if len(toRemove) == 0 {
			break
		}
		
		// Remove all accessible rolls
		for _, pos := range toRemove {
			state[pos[0]][pos[1]] = '.'
			totalRemoved++
		}
	}
	
	return totalRemoved
}

func main() {
	result1 := part1()
	result2 := part2()
	fmt.Printf("Part 1: %d\n", result1)
	fmt.Printf("Part 2: %d\n", result2)
}

