package common

import (
    "bufio"
    "os"
)

func ReadLines(path string) []string {
	file, _ := os.Open(path)
	defer file.Close()

    var lines []string
	for scanner := bufio.NewScanner(file); scanner.Scan(); {
		lines = append(lines, scanner.Text())
	}

	return lines
}
