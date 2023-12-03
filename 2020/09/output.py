# Part 1
def find_sum_of_two(numbers, target_sum):
    previously_seen_entries = set()

    for number in numbers:
        complement = target_sum - number
        if complement in previously_seen_entries:
            return True
        else:
            previously_seen_entries.add(number)

    return False

def get_numbers(file_name):
    with open(file_name) as file:
        return [int(line) for line in file.read().splitlines()]

def find_wrong_number(numbers):
    PREAMBLE_LENGTH = 25

    for index in range(PREAMBLE_LENGTH, len(numbers)):
        if not find_sum_of_two(
            numbers[index - PREAMBLE_LENGTH : index], numbers[index]
        ):
            return numbers[index]

print(find_wrong_number(get_numbers("input.txt")))

# Part 2
def find_encryption_weakness(file_name):
    MIN_CONTIGUOUS_LENGTH = 2
    START = 0

    numbers = get_numbers(file_name)
    wrong_number = find_wrong_number(numbers)

    sum = 0
    tail = START  # (inclusive)
    head = START  # (exclusive)

    found_weakness = False

    while tail < len(numbers):
        while head - tail < MIN_CONTIGUOUS_LENGTH:
            head += 1
            sum += numbers[head - 1]

        if sum < wrong_number:
            while sum < wrong_number:
                head += 1
                sum += numbers[head - 1]

        elif sum > wrong_number:
            while sum > wrong_number and head - tail > MIN_CONTIGUOUS_LENGTH:
                sum -= numbers[head - 1]
                head -= 1

        if sum == wrong_number:
            found_weakness = True
            break

        sum -= numbers[tail]
        tail += 1

    assert found_weakness

    smallest = None
    largest = None
    for index in range(tail, head):
        if smallest is None or smallest > numbers[index]:
            smallest = numbers[index]

        if largest is None or largest < numbers[index]:
            largest = numbers[index]

    return smallest + largest

print(find_encryption_weakness("input.txt"))
