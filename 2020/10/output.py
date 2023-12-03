MAX_JOLT_DIFFERENCE = 3

def get_numbers(file_name):
    with open(file_name) as file:
        return [int(line) for line in file.read().splitlines()]

# Part 1
def get_multiplied_jolt_differences(numbers):
    jolt_differences = [0] * MAX_JOLT_DIFFERENCE
    # (`jolt_difference - 1` gives the index of this array)

    numbers.sort()
    numbers.insert(0, 0)
    numbers.append(numbers[-1] + MAX_JOLT_DIFFERENCE)

    for index in range(1, len(numbers)):
        jolt_differences[numbers[index] - numbers[index - 1] - 1] += 1

    return jolt_differences[1 - 1] * jolt_differences[3 - 1]

print(get_multiplied_jolt_differences(get_numbers("input.txt")))

# Part 2
#
# Works from right-to-left, adding up all arrangements at each step.
def get_num_arrangements(numbers):
    numbers.sort()
    numbers.insert(0, 0)
    numbers.append(numbers[-1] + MAX_JOLT_DIFFERENCE)

    num_arrangements = {}
    num_arrangements[numbers[-1]] = 1

    for reverse_index in range(len(numbers) - 2, -1, -1):
        # (O(N))
        num_arrangements[numbers[reverse_index]] = 0

        for forward_index in range(reverse_index + 1, len(numbers)):
            # (O(MAX_JOLT_DIFFERENCE) -> O(1))
            if numbers[forward_index] - numbers[reverse_index] <= MAX_JOLT_DIFFERENCE:
                num_arrangements[numbers[reverse_index]] += num_arrangements[
                    numbers[forward_index]
                ]

    return num_arrangements[0]

print(get_num_arrangements(get_numbers("input.txt")))
