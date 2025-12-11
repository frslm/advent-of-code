package main

import (
	"2025/common"
	"fmt"
	"slices"
	"sort"
	"strconv"
	"strings"
	"time"
)

type button []int

type machine struct {
	indicatorTarget []bool
	buttons         []button
	joltagesTarget  []int
}

func between(snippet, start, end string) string {
	return snippet[strings.Index(snippet, start)+len(start) : strings.Index(snippet, end)]
}

func readManual() []machine {
	machines := []machine{}
	for _, line := range common.ReadLines("input.txt") {
		machine := machine{}

		for _, light := range between(line, "[", "]") {
			var lit bool
			switch light {
			case '.':
				lit = false
			case '#':
				lit = true
			}
			machine.indicatorTarget = append(machine.indicatorTarget, lit)
		}

		for _, snippet := range strings.Split(between(line, "] ", " {"), " ") {
			button := button{}
			for _, light := range strings.Split(between(snippet, "(", ")"), ",") {
				index, _ := strconv.Atoi(light)
				button = append(button, index)
			}
			machine.buttons = append(machine.buttons, button)
		}

		for _, joltageAsString := range strings.Split(between(line, "{", "}"), ",") {
			joltage, _ := strconv.Atoi(joltageAsString)
			machine.joltagesTarget = append(machine.joltagesTarget, joltage)
		}

		machines = append(machines, machine)
	}
	return machines
}

func (machine machine) fewestPressesToReachIndicatorTarget(state []bool, pressesSoFar int, nextButton int) (int, bool) {
	if slices.Equal(state, machine.indicatorTarget) {
		return pressesSoFar, true
	} else if nextButton < len(machine.buttons) {
		// Skip button:
		valueSkip, okSkip := machine.fewestPressesToReachIndicatorTarget(slices.Clone(state), pressesSoFar, nextButton+1)

		// Press button:
		nextState := slices.Clone(state)
		for _, index := range machine.buttons[nextButton] {
			nextState[index] = !nextState[index]
		}
		valuePress, okPress := machine.fewestPressesToReachIndicatorTarget(nextState, pressesSoFar+1, nextButton+1)

		// Return the smaller number of presses:
		if okSkip && okPress {
			return min(valueSkip, valuePress), true
		} else if okSkip && !okPress {
			return valueSkip, true
		} else if !okSkip && okPress {
			return valuePress, true
		} else {
			return 0, false
		}
	} else {
		return 0, false
	}
}

type equation []int // (first is the target, the rest are the button coefficients)
type equations []equation

func (machine machine) buildEquations() equations {
	equations := equations{}
	for joltageIndex, joltage := range machine.joltagesTarget {
		equation := make(equation, 1+len(machine.buttons))
		equation[0] = joltage

		for buttonIndex, button := range machine.buttons {
			for _, joltageReference := range button {
				if joltageReference == joltageIndex {
					equation[1+buttonIndex] = 1
					break
				}
			}
		}

		equations = append(equations, equation)
	}
	return equations
}

func (equation equation) numCoefficients() int {
	coefficients := 0
	for _, coefficient := range equation[1:] {
		if coefficient != 0 {
			coefficients++
		}
	}
	return coefficients
}

func (equations equations) sortBySolvable() {
	sort.Slice(equations, func(i, j int) bool {
		diff := equations[j].numCoefficients() - equations[i].numCoefficients()
		if diff > 0 {
			return true
		} else if diff < 0 {
			return false
		} else {
			return equations[i][0] < equations[j][0] // (optimization - favour the smaller target)
		}
	})
}

func (equations equations) equationsContradict() bool {
	targetMap := map[string]int{}

	for _, equation := range equations {
		coefficients := equation[1:]

		bytes := make([]byte, len(coefficients))
		for index, coefficient := range coefficients {
			bytes[index] = byte(coefficient)
		}
		coefficientString := string(bytes)

		target, exists := targetMap[coefficientString]

		if !exists {
			targetMap[coefficientString] = equation[0]
		} else if target != equation[0] {
			return true // (two identical coefficients can't have different targets - unsolveable)
		}
	}

	return false
}

func (equations equations) indexOfBestCoefficientForEquation(i int) int {
	indexFrequency := [][2]int{}
	for index, coefficient := range equations[i][1:] {
		if coefficient == 1 {
			count := 0
			for _, eq := range equations {
				if eq[1+index] == 1 {
					count++
				}
			}
			indexFrequency = append(indexFrequency, [2]int{index, count})
		}
	}

	sort.Slice(indexFrequency, func(i, j int) bool { return indexFrequency[i][1] < indexFrequency[j][1] })
	// (small optimization - prefer choosing the index with fewer appearances in other equations)

	return indexFrequency[0][0]
}

func (equations equations) deepCopy() equations {
	newEquations := []equation{}
	for _, equation := range equations {
		newEquations = append(newEquations, slices.Clone(equation))
	}
	return newEquations
}

func fewestPressesToReachJoltagesTarget(equations equations, pressesSoFar int, fewestPressesGlobally *int) {
	if *fewestPressesGlobally != -1 && pressesSoFar >= *fewestPressesGlobally {
		return // (optimization - exit early on longer solutions)
	} else if len(equations) == 0 {
		*fewestPressesGlobally = pressesSoFar
	} else if equations.equationsContradict() {
		return // (big optimization - exit early if equations have a contradiction)
	} else {
		equations.sortBySolvable() // (big optimization - deal with the more-known values first)

		equation := equations[0]
		target := equation[0]
		index := equations.indexOfBestCoefficientForEquation(0)

		start := 0
		if equation.numCoefficients() == 1 {
			start = target
		}

		// (small optimization - skip cases where the target can force the value to go negative)
		for _, equation := range equations {
			if equation[1+index] == 1 && equation[0] < target {
				target = equation[0]
			}
		}

		for value := start; value <= target; value++ {
			newEquations := equations.deepCopy()

			// Substitute the value into each equation, ensuring that no target goes negative:
			for _, eq := range newEquations {
				if eq[1+index] == 1 {
					eq[1+index] = 0
					eq[0] -= value
					if eq[0] < 0 {
						panic("Expected only non-negative targets since the max was capped above!")
					}
				}
			}

			// (small optimization - remove all coefficients associated with a target of 0)
			for _, eq := range newEquations {
				if eq[0] == 0 {
					coefficientsToPurge := slices.Clone(eq[1:])
					for _, otherEq := range newEquations {
						for i, coefficient := range coefficientsToPurge {
							if coefficient == 1 {
								otherEq[1+i] = 0
							}
						}
					}
				}
			}

			// Remove "fully consumed" equations:
			skip := false
			for i := 0; i < len(newEquations); {
				if newEquations[i].numCoefficients() == 0 {
					if newEquations[i][0] == 0 {
						newEquations = append(newEquations[:i], newEquations[i+1:]...)
					} else {
						skip = true // (non-zero target, but no coefficients remain to control it)
						break
					}
				} else {
					i++
				}
			}
			if skip {
				continue
			}

			fewestPressesToReachJoltagesTarget(newEquations, pressesSoFar+value, fewestPressesGlobally)
		}
	}
}

func part1() {
	// Couple ideas here:
	// 1. Build a state graph where each edge is a button press, then traverse
	//    through this graph to find the shortest path to the target.
	// 2. Toggling the same button twice is useless, so each button should be
	//    toggled either never or once, so use dynamic programming to call a
	//    subset of the state with the previous button either pressed or not.

	fewestTotalPresses := 0
	for _, machine := range readManual() {
		fewestPresses, ok := machine.fewestPressesToReachIndicatorTarget(make([]bool, len(machine.indicatorTarget)), 0, 0)
		if ok {
			fewestTotalPresses += fewestPresses
		}
	}
	fmt.Println(fewestTotalPresses)
}

func part2() {
	// Rolled a custom wonky solver, adding enough optimizations until it finished on time.
	// Most equations solve quickly, except for those hardcoded below; those are skipped in
	// the batch run, and instead run individually while monitoring their smallest value
	// so far.

	slow := map[int]bool{
		55:  false, // ~8s, answer: 210
		99:  false, // does not finish, but smallest answer so far was: 272
		126: false, // ~50s, answer: 105
		131: false, // ~1m25s, answer: 112
		184: false, // ~2m46s, answer: 106
	}

	// Batch results (skipping the above) sum to 20664.
	// Batch results (including the above) sum to 21469.

	startTotal := time.Now()
	fewestTotalPresses := 0
	for i, machine := range readManual() {
		_, exists := slow[i+1]
		if !exists {
			fewestPresses := -1

			start := time.Now()
			fewestPressesToReachJoltagesTarget(machine.buildEquations(), 0, &fewestPresses)
			elapsed := time.Since(start)
			if fewestPresses != -1 {
				fewestTotalPresses += fewestPresses
			} else {
				panic("Expected to always find at least one solution!")
			}

			fmt.Println(i+1, fewestPresses, elapsed)
		} else {
			fmt.Println(i+1, "skipped")
		}
	}
	elapsedTotal := time.Since(startTotal)
	fmt.Println("Total", fewestTotalPresses, elapsedTotal)
}

func main() {
	part1()
	part2()
}
