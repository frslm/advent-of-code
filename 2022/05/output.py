import re

with open("input.txt") as input:
    # Calculate number of stacks:
    num_stacks = (len(next(input).rstrip("\n")) + 1) // 4
    input.seek(0)  # (reset to top, since we just consumed the first line)
    # (each stack is 3 characters wide, separated by a single space, so total length is 3*N + N-1, where N is number of stacks)

    # Build the initial stack setup:
    stacks: list[list[str]] = [[] for _ in range(num_stacks)]
    stack_axis = " ".join([f" {num} " for num in range(1, 1 + num_stacks)])

    for line in input:
        line = line.rstrip("\n")
        if line == stack_axis:
            break  # (reached the end of the stack representation)

        for num in range(num_stacks):
            if (crate := line[1 + 4 * num]) != " ":
                stacks[num].insert(0, crate)

    stacks_chunked = [list(stack) for stack in stacks]

    # Skip the blank line between the stack representation and the actual instructions
    divider = next(input).strip()
    assert divider == ""

    # Parse stack transfers:
    for line in input:  # move 7 from 6 to 8
        match = re.match(r"move (\d+) from (\d+) to (\d+)", line.strip())
        assert match is not None
        quantity, source, destination = [int(string) for string in match.groups()]

        for _ in range(quantity):
            stacks[destination - 1].append(stacks[source - 1].pop())

        stacks_chunked[destination - 1].extend(stacks_chunked[source - 1][-quantity:])
        stacks_chunked[source - 1] = stacks_chunked[source - 1][:-quantity]

    # Return the top-most crate from all stacks:
    # Part 1
    print("".join([stack[-1] for stack in stacks]))

    # Part 2
    print("".join([stack[-1] for stack in stacks_chunked]))
