def _priority(item: str) -> int:
    if ord("a") <= ord(item) <= ord("z"):
        return ord(item) - ord("a") + 1
    else:
        assert ord("A") <= ord(item) <= ord("Z")
        return ord(item) - ord("A") + 27


# Part 1
total_priority = 0

with open("input.txt") as input:
    for line in input:
        line = line.strip()

        assert len(line) % 2 == 0
        # (guaranteed that each compartment has the same number of items)

        first_compartment, second_compartment = (
            line[: len(line) // 2],
            set(line[len(line) // 2 :]),
        )

        for item in first_compartment:
            if item in second_compartment:
                break

        total_priority += _priority(item)


print(total_priority)

# Part 2
GROUP_SIZE = 3
Group = list[str]

total_priority = 0

with open("input.txt") as input:
    group: Group = []

    for line in input:
        group.append(line.strip())

        if len(group) == GROUP_SIZE:
            possible_items = set(group[0])

            for rucksack in group[1:]:
                new_items = set(rucksack)
                possible_items = {item for item in possible_items if item in new_items}

            assert len(possible_items) == 1
            total_priority += _priority(list(possible_items)[0])

            group = []


print(total_priority)
