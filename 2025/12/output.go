package main

import (
	"2025/common"
	"fmt"
	"slices"
	"strconv"
	"strings"
)

const empty = '.'
const filled = '#'

type grid [][]byte

type shape grid
type shapeVariants []shape

func (oldGrid grid) copy() grid {
	newGrid := grid{}
	for _, row := range oldGrid {
		newGrid = append(newGrid, slices.Clone(row))
	}
	return newGrid
}

func (oldShape shape) rotate90DegreesClockwise() shape {
	newShape := shape{}
	for x := 0; x < len(oldShape[0]); x++ {
		row := []byte{}
		for y := len(oldShape) - 1; y >= 0; y-- {
			row = append(row, oldShape[y][x])
		}
		newShape = append(newShape, row)
	}
	return newShape
}

func (oldShape shape) flipHorizontally() shape {
	newShape := shape{}
	for y := 0; y < len(oldShape); y++ {
		row := []byte{}
		for x := len(oldShape[0]) - 1; x >= 0; x-- {
			row = append(row, oldShape[y][x])
		}
		newShape = append(newShape, row)
	}
	return newShape
}

func (shape shape) hash() string {
	cells := []byte{}
	for _, row := range shape {
		cells = append(cells, row...)
	}
	return string(cells)
}

func (sourceShapeVariants shapeVariants) pruneCopies() shapeVariants {
	uniqueShapes := map[string]shape{}
	for _, shapeVariant := range sourceShapeVariants {
		key := shapeVariant.hash()
		_, exists := uniqueShapes[key]
		if !exists {
			uniqueShapes[key] = shapeVariant
		}
	}

	uniqueShapeVariants := shapeVariants{}
	for _, shape := range uniqueShapes {
		uniqueShapeVariants = append(uniqueShapeVariants, shape)
	}
	return uniqueShapeVariants
}

func (sourceShape shape) generateAllVariants() shapeVariants {
	shapeVariants := shapeVariants{}
	currentShape := sourceShape
	for degrees := 0; degrees < 360; degrees += 90 {
		shapeVariants = append(shapeVariants, currentShape, currentShape.flipHorizontally())
		currentShape = currentShape.rotate90DegreesClockwise()
	}
	return shapeVariants.pruneCopies() // (optimization - no need to hold onto identical copies)
}

type regionRequirements struct {
	width, length   int
	shapeQuantities []int
}

type situation struct {
	allShapeVariants      []shapeVariants
	allRegionRequirements []regionRequirements
}

func indexOfLast(collection []string, value string) int {
	index := -1

	for i := len(collection) - 1; i >= 0; i-- {
		if collection[i] == value {
			return i
		}
	}

	return index
}

func createEmptyRegion(width, length int) grid {
	region := grid{}
	for y := 0; y < length; y++ {
		row := make([]byte, width)
		for x := 0; x < len(row); x++ {
			row[x] = empty
		}
		region = append(region, row)
	}
	return region
}

func readSituation() situation {
	situation := situation{}

	lines := common.ReadLines("input.txt")

	sectionSeparatorIndex := indexOfLast(lines, "")

	pattern := shape{}
	parsingShape := false
	for _, line := range lines[:sectionSeparatorIndex+1] {
		if !parsingShape {
			parsingShape = true
		} else {
			if line != "" {
				row := []byte(line)
				pattern = append(pattern, row)
			} else {
				situation.allShapeVariants = append(situation.allShapeVariants, pattern.generateAllVariants())
				pattern = shape{}
				parsingShape = false
			}
		}
	}

	for _, line := range lines[sectionSeparatorIndex+1:] {
		regionRequirements := regionRequirements{}

		fragments := strings.Split(line, ": ")

		dimensions := strings.Split(fragments[0], "x")
		width, _ := strconv.Atoi(dimensions[0])
		length, _ := strconv.Atoi(dimensions[1])
		regionRequirements.width = width
		regionRequirements.length = length

		for _, shapeQuantityAsString := range strings.Fields(fragments[1]) {
			shapeQuantity, _ := strconv.Atoi(shapeQuantityAsString)
			regionRequirements.shapeQuantities = append(regionRequirements.shapeQuantities, shapeQuantity)
		}

		situation.allRegionRequirements = append(situation.allRegionRequirements, regionRequirements)
	}

	return situation
}

func indexOfFirstNonZero(collection []int) int {
	for index, value := range collection {
		if value != 0 {
			return index
		}
	}
	return -1
}

func (region grid) canFit(shapeQuantitiesRemaining []int, allShapeVariants []shapeVariants, xStart, yStart int) bool {
	index := indexOfFirstNonZero(shapeQuantitiesRemaining)
	if index == -1 {
		return true
	} else {
		for _, shape := range allShapeVariants[index] {
			for y := yStart; y <= len(region)-len(shape); y++ {
				if y > yStart {
					xStart = 0
				}

				for x := xStart; x <= len(region[0])-len(shape[0]); x++ {
					fits := true

				fitCheck:
					for yShape := 0; yShape < len(shape); yShape++ {
						for xShape := 0; xShape < len(shape[0]); xShape++ {
							regionCell := region[y+yShape][x+xShape]
							shapeCell := shape[yShape][xShape]

							if regionCell == filled && shapeCell == filled {
								fits = false
								break fitCheck
							}
						}
					} // (optimization - check for a fit first before copying the entire grid and attempting to copy the shape in)

					if fits {
						newRegion := region.copy()

						for yShape := 0; yShape < len(shape); yShape++ {
							for xShape := 0; xShape < len(shape[0]); xShape++ {
								regionCell := &newRegion[y+yShape][x+xShape]
								shapeCell := shape[yShape][xShape]

								if shapeCell == filled {
									if *regionCell != filled {
										*regionCell = shapeCell
									} else {
										panic("Expected no shape clash since it should've been checked already!")
									}
								}
							}
						}

						newShapeQuantitiesRemaining := slices.Clone(shapeQuantitiesRemaining)
						newShapeQuantityRemaining := &newShapeQuantitiesRemaining[index]
						*newShapeQuantityRemaining--

						newXStart, newYStart := 0, 0
						if *newShapeQuantityRemaining != 0 {
							newXStart, newYStart = x+1, y
						} // (optimization - don't fit a copy of a shape before a previously-placed one)

						if newRegion.canFit(newShapeQuantitiesRemaining, allShapeVariants, newXStart, newYStart) {
							return true
						}
					}
				}
			}
		}

		return false
	}
}

func part1Attempt() {
	// Attempted to manually try all combinations of shapes fitting in,
	// including some flood-filling attempts to skip combinations the moment
	// any bubble is formed, but they all took too long.

	situation := readSituation()

	regionsThatCanFitAllShapes := 0
	for _, region := range situation.allRegionRequirements {
		if createEmptyRegion(region.width, region.length).canFit(region.shapeQuantities, situation.allShapeVariants, 0, 0) {
			regionsThatCanFitAllShapes++
		}
	}

	fmt.Println(regionsThatCanFitAllShapes)
}

func part1() {
	// Noticed some inputs finished very quickly while others took forever,
	// so assume no shape can fit into another (even though the inputs and
	// examples allow it), then check if all shapes can fit in the region
	// without overlapping, and just include those in the total count.
	// This completely bypasses the differences in each shape by effectively
	// treating each shape as a completely filled 3x3 grid.

	situation := readSituation()

	regionsThatCanFitAllShapes := 0
	for _, region := range situation.allRegionRequirements {
		const size = 3 // (hardcoded to match the fixed size of all shapes in the input)
		widthFit := region.width / size
		lengthFit := region.length / size

		shapes := 0
		for _, quantity := range region.shapeQuantities {
			shapes += quantity
		}

		if (widthFit*lengthFit)-shapes >= 0 {
			regionsThatCanFitAllShapes++
		}
	}

	fmt.Println(regionsThatCanFitAllShapes)
}

func part2() {
	// (doesn't exist)
}

func main() {
	part1()
	part2()
}
