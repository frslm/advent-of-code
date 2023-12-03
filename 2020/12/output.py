NORTH = "N"
SOUTH = "S"
EAST = "E"
WEST = "W"

ROTATION_IN_DEGREES = 90

LEFT = "L"
RIGHT = "R"
FORWARD = "F"

class EastingNorthing:
    def __init__(self, east, north):
        self.east = east
        self.north = north

# Part 1
def get_destination_manhattan_distance(file_name):
    CLOCKWISE_ROTATIONS = [NORTH, EAST, SOUTH, WEST]

    position = EastingNorthing(0, 0)
    heading = EAST

    with open(file_name) as file:
        for line in file:
            action = line[0]
            value = int(line.strip()[1:])

            if action == FORWARD:
                action = heading

            if action == NORTH:
                position.north += value
            elif action == SOUTH:
                position.north -= value
            elif action == EAST:
                position.east += value
            elif action == WEST:
                position.east -= value
            elif action == LEFT:
                heading = CLOCKWISE_ROTATIONS[
                    (CLOCKWISE_ROTATIONS.index(heading) - value // ROTATION_IN_DEGREES)
                    % len(CLOCKWISE_ROTATIONS)
                ]
            elif action == RIGHT:
                heading = CLOCKWISE_ROTATIONS[
                    (CLOCKWISE_ROTATIONS.index(heading) + value // ROTATION_IN_DEGREES)
                    % len(CLOCKWISE_ROTATIONS)
                ]

    return abs(position.east) + abs(position.north)

print(get_destination_manhattan_distance("input.txt"))

# Part 2
SIN_VALUES = [0, 1, 0, -1]

def discrete_sin(angle):
    return SIN_VALUES[(angle // ROTATION_IN_DEGREES) % len(SIN_VALUES)]

def discrete_cos(angle):
    return SIN_VALUES[
        ((angle + ROTATION_IN_DEGREES) // ROTATION_IN_DEGREES) % len(SIN_VALUES)
    ]

def rotate_clockwise(point, heading):
    return EastingNorthing(
        point.east * discrete_cos(heading) + point.north * discrete_sin(heading),
        point.east * -discrete_sin(heading) + point.north * discrete_cos(heading),
    )

def get_waypointed_destination_manhattan_distance(file_name):
    ship = EastingNorthing(0, 0)
    waypoint = EastingNorthing(10, 1)  # (always relative to ship)

    with open(file_name) as file:
        for line in file:
            action = line[0]
            value = int(line.strip()[1:])

            if action == NORTH:
                waypoint.north += value
            elif action == SOUTH:
                waypoint.north -= value
            elif action == EAST:
                waypoint.east += value
            elif action == WEST:
                waypoint.east -= value
            elif action == LEFT:
                waypoint = rotate_clockwise(waypoint, -value)
            elif action == RIGHT:
                waypoint = rotate_clockwise(waypoint, value)
            elif action == FORWARD:
                ship.east += waypoint.east * value
                ship.north += waypoint.north * value

    return abs(ship.east) + abs(ship.north)

print(get_waypointed_destination_manhattan_distance("input.txt"))
