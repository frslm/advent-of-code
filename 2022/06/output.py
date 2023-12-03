def _marker_index(marker_size: int) -> int:
    with open("input.txt") as input:
        line = next(input)

        chars = list(line[:marker_size])
        chars_parsed = len(chars)

        for char in line[marker_size:]:
            if len(set(chars)) == marker_size:
                break

            chars.append(char)
            chars.pop(0)
            chars_parsed += 1

    return chars_parsed

# Part 1
print(_marker_index(4))

# Part 2
print(_marker_index(14))
