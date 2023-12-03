# Part 1
def find_earliest_depart_time(file_name):
    with open(file_name) as file:
        input = file.read().splitlines()

        depart_time = int(input[0])
        ids = [int(val) for val in input[1].split(",") if val != "x"]

        earliest_id = None
        shortest_wait_time = None
        for id in ids:
            wait_time = id - (depart_time % id)
            if shortest_wait_time is None or wait_time < shortest_wait_time:
                earliest_id = id
                shortest_wait_time = wait_time

    return earliest_id * shortest_wait_time

print(find_earliest_depart_time("input.txt"))

# Part 2
class DepartDetails:
    def __init__(self, id, wait_time):
        self.id = id
        self.wait_time = wait_time

# From: https://en.wikipedia.org/wiki/Euclidean_algorithm#Implementations
def greatest_common_divisor(lhs, rhs):
    while rhs != 0:
        temp = rhs
        rhs = lhs % rhs
        lhs = temp

    return lhs

# From: https://stackoverflow.com/a/3154503/8616205
def lowest_common_multiple(lhs, rhs):
    return lhs * rhs // greatest_common_divisor(lhs, rhs)

def find_consecutive_depart_time(file_name):
    with open(file_name) as file:
        departures = [
            DepartDetails(int(id), index)
            for index, id in enumerate(file.read().splitlines()[1].split(","))
            if id != "x"
        ]

    period = 1
    offset = 0

    for departure in departures:
        i = 0
        while True:
            time = period * i + offset
            if (time + departure.wait_time) % departure.id == 0:
                break

            i += period

        period = lowest_common_multiple(period, departure.id)
        offset = time % period

    N = 0  # (want the very first time we get consecutive departures)
    return period * N + offset

print(find_consecutive_depart_time("input.txt"))
