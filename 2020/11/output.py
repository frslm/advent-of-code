def read_seats(file_name):
    with open(file_name) as file:
        return [list(line) for line in file.read().splitlines()]

SEAT = "L"
OCCUPIED = "#"
FLOOR = "."

class Neighbours:
    def __init__(self):
        self.seat = 0
        self.occupied = 0
        self.floor = 0

    def add(self, neighbour):
        if neighbour == SEAT:
            self.seat += 1
        elif neighbour == OCCUPIED:
            self.occupied += 1
        elif neighbour == FLOOR:
            self.floor += 1
        else:
            raise "Unknown type of neighbour!"

def get_adjacent_neighbours(seats, row, col):
    neighbours = Neighbours()

    have_top = row > 0
    have_bottom = row < len(seats) - 1
    have_left = col > 0
    have_right = col < len(seats[0]) - 1

    if have_top:
        neighbours.add(seats[row - 1][col])
    if have_bottom:
        neighbours.add(seats[row + 1][col])
    if have_left:
        neighbours.add(seats[row][col - 1])
    if have_right:
        neighbours.add(seats[row][col + 1])
    if have_top and have_left:
        neighbours.add(seats[row - 1][col - 1])
    if have_top and have_right:
        neighbours.add(seats[row - 1][col + 1])
    if have_bottom and have_left:
        neighbours.add(seats[row + 1][col - 1])
    if have_bottom and have_right:
        neighbours.add(seats[row + 1][col + 1])

    return neighbours

def get_cardinal_neighbours(seats, row, col):
    neighbours = Neighbours()

    # Top:
    for r in range(row - 1, -1, -1):
        seat = seats[r][col]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Bottom:
    for r in range(row + 1, len(seats)):
        seat = seats[r][col]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Left:
    for c in range(col - 1, -1, -1):
        seat = seats[row][c]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Right:
    for c in range(col + 1, len(seats[0])):
        seat = seats[row][c]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Top-left:
    for offset in range(1, min(row - 0, col - 0) + 1):
        seat = seats[row - offset][col - offset]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Top-right:
    for offset in range(1, min(row - 0, len(seats[0]) - 1 - col) + 1):
        seat = seats[row - offset][col + offset]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Bottom-left:
    for offset in range(1, min(len(seats) - 1 - row, col - 0) + 1):
        seat = seats[row + offset][col - offset]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    # Bottom-right:
    for offset in range(1, min(len(seats) - 1 - row, len(seats[0]) - 1 - col) + 1):
        seat = seats[row + offset][col + offset]
        if seat != FLOOR:
            neighbours.add(seat)
            break

    return neighbours

def simulate_one_step(seats, check_cardinal, occupied_before_leaving):
    original_seats = [row[:] for row in seats]

    modified = False

    for row in range(0, len(original_seats)):
        for col in range(0, len(original_seats[0])):
            neighbours = (
                get_adjacent_neighbours(original_seats, row, col)
                if not check_cardinal
                else get_cardinal_neighbours(original_seats, row, col)
            )

            if original_seats[row][col] == SEAT and neighbours.occupied == 0:
                seats[row][col] = OCCUPIED
                modified = True
            elif (
                original_seats[row][col] == OCCUPIED
                and neighbours.occupied >= occupied_before_leaving
            ):
                seats[row][col] = SEAT
                modified = True

    return modified

def simulate_till_no_change(seats, check_cardinal=False, occupied_before_leaving=4):
    while simulate_one_step(seats, check_cardinal, occupied_before_leaving):
        pass

    return sum([1 if seat == OCCUPIED else 0 for row in seats for seat in row])

# Part 1
print(simulate_till_no_change(read_seats("input.txt"), False, 4))

# Part 2
print(simulate_till_no_change(read_seats("input.txt"), True, 5))
