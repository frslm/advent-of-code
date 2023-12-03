fully_contained_assignments = 0
contained_assignments = 0

with open("input.txt") as input:
    for line in input:
        first, second = [
            tuple([int(section) for section in assignment.split("-")])
            for assignment in line.strip().split(",")
        ]

        for source, destination in [(first, second), (second, first)]:
            if source[0] >= destination[0] and source[1] <= destination[1]:
                fully_contained_assignments += 1
                break

        for source, destination in [(first, second), (second, first)]:
            if (
                destination[0] <= source[0] <= destination[1]
                or destination[0] <= source[1] <= destination[1]
            ):
                contained_assignments += 1
                break


# Part 1
print(fully_contained_assignments)

# Part 2
print(contained_assignments)
