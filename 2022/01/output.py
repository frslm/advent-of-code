# Part 1
calories: list[int] = [0]

with open("input.txt") as input:
    for line in input:
        line = line.strip()
        if line != "":
            calories[-1] += int(line)
        else:
            calories.append(0)

print(max(calories))

# Part 2
print(sum(sorted(calories, reverse=True)[:3]))
