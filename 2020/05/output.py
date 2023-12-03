FRONT = "F"
BACK = "B"
LEFT = "L"
RIGHT = "R"

NUM_FRONT_BACK = 7
NUM_LEFT_RIGHT = 3

def get_seat_id(binary_partition):
    row_range = [0, 2 ** NUM_FRONT_BACK - 1]
    col_range = [0, 2 ** NUM_LEFT_RIGHT - 1]

    for symbol in binary_partition:
        if symbol == FRONT or symbol == BACK:
            sum = row_range[1] + row_range[0]
            if symbol == FRONT:
                row_range[1] = (sum) // 2
            elif symbol == BACK:
                row_range[0] = sum // 2 + sum % 2
        elif symbol == LEFT or symbol == RIGHT:
            sum = col_range[1] + col_range[0]
            if symbol == LEFT:
                col_range[1] = sum // 2
            elif symbol == RIGHT:
                col_range[0] = sum // 2 + sum % 2

    return row_range[0] * (2 ** NUM_LEFT_RIGHT) + col_range[0]

def get_smallest_seat_id(file_name):
    min_seat_id = (2 ** NUM_FRONT_BACK - 1) * (2 ** NUM_LEFT_RIGHT) + (
        2 ** NUM_LEFT_RIGHT - 1
    )

    with open(file_name) as input:
        for line in input:
            min_seat_id = min(min_seat_id, get_seat_id(line.strip()))

    return min_seat_id

# Part 1
def get_largest_seat_id(file_name):
    max_seat_id = 0

    with open(file_name) as input:
        for line in input:
            max_seat_id = max(max_seat_id, get_seat_id(line.strip()))

    return max_seat_id

print(get_largest_seat_id("input.txt"))

# Part 2
def find_missing_seat_id(file_name):
    smallest_seat_id = get_smallest_seat_id(file_name)
    largest_seat_id = get_largest_seat_id(file_name)

    seat_ids = set(range(smallest_seat_id, largest_seat_id + 1))

    with open(file_name) as input:
        for line in input:
            seat_ids.remove(get_seat_id(line.strip()))

    return list(seat_ids)[0]

print(find_missing_seat_id("input.txt"))
