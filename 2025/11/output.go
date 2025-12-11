package main

import (
	"2025/common"
	"fmt"
	"strings"
)

type graph map[string][]string

func readGraph() graph {
	graph := graph{}
	for _, line := range common.ReadLines("input.txt") {
		sections := strings.Split(line, ": ")
		graph[sections[0]] = strings.Split(sections[1], " ")
	}
	return graph
}

func (graph graph) traverse(location, target string, memory map[string]int64) int64 {
	cachedPaths, exists := memory[location]

	if exists {
		return cachedPaths
	} else {
		paths := int64(0)

		if location == target {
			paths = 1
		} else {
			for _, output := range graph[location] {
				paths += graph.traverse(output, target, memory)
			}
		}

		memory[location] = paths

		return paths
	}
}

func part1() {
	fmt.Println(readGraph().traverse("you", "out", map[string]int64{}))
}

func part2() {
	// Assumes no loops, meaning one of the nodes must always come before the other:

	//       /----\               /----\               /----\
	// svr ----------> [node 1] ----------> [node 2] ----------> out
	//       \----/               \----/               \----/

	// Those nodes must act as checkpoints; all valid paths must go through them in order.

	// So instead of traversing the entire path and checking if it contained the nodes of
	// interest, traverse the three sections separately, then multiply the number of paths
	// in each section with each other to get the total number of paths.

	// All that's left to do is to determine which node comes before the other, so traverse
	// from the start to each of the nodes; the node with way fewer paths to it must come
	// before the other.

	graph := readGraph()
	paths := int64(0)

	svrToDac := graph.traverse("svr", "dac", map[string]int64{})
	svrToFft := graph.traverse("svr", "fft", map[string]int64{})

	if svrToDac < svrToFft {
		paths = svrToDac *
			graph.traverse("dac", "fft", map[string]int64{}) *
			graph.traverse("fft", "out", map[string]int64{})
	} else {
		paths = svrToFft *
			graph.traverse("fft", "dac", map[string]int64{}) *
			graph.traverse("dac", "out", map[string]int64{})
	}

	fmt.Println(paths)
}

func main() {
	part1()
	part2()
}
