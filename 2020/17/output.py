ACTIVE = "#"
INACTIVE = "."

def create_1d_space(width):
    return [INACTIVE for col in range(width)]

def create_2d_space(width, height):
    return [create_1d_space(width) for row in range(height)]

def create_3d_space(width, height, depth):
    return [create_2d_space(width, height) for level in range(depth)]

def create_4d_space(width, height, depth, w_size):
    return [create_3d_space(width, height, depth) for w in range(w_size)]

def get_1d_adjacent_active(space, col):
    num_adjacent_active = 0

    if col > 0 and space[col - 1] == ACTIVE:
        num_adjacent_active += 1

    if col < len(space) - 1 and space[col + 1] == ACTIVE:
        num_adjacent_active += 1

    return num_adjacent_active

def get_2d_adjacent_active(space, row, col):
    num_adjacent_active = get_1d_adjacent_active(space[row], col)

    if row > 0:
        num_adjacent_active += get_1d_adjacent_active(space[row - 1], col)
        if space[row - 1][col] == ACTIVE:
            num_adjacent_active += 1

    if row < len(space) - 1:
        num_adjacent_active += get_1d_adjacent_active(space[row + 1], col)
        if space[row + 1][col] == ACTIVE:
            num_adjacent_active += 1

    return num_adjacent_active

def get_3d_adjacent_active(space, level, row, col):
    num_adjacent_active = get_2d_adjacent_active(space[level], row, col)

    if level > 0:
        num_adjacent_active += get_2d_adjacent_active(space[level - 1], row, col)
        if space[level - 1][row][col] == ACTIVE:
            num_adjacent_active += 1

    if level < len(space) - 1:
        num_adjacent_active += get_2d_adjacent_active(space[level + 1], row, col)
        if space[level + 1][row][col] == ACTIVE:
            num_adjacent_active += 1

    return num_adjacent_active

def get_4d_adjacent_active(space, w, level, row, col):
    num_adjacent_active = get_3d_adjacent_active(space[w], level, row, col)

    if w > 0:
        num_adjacent_active += get_3d_adjacent_active(space[w - 1], level, row, col)
        if space[w - 1][level][row][col] == ACTIVE:
            num_adjacent_active += 1

    if w < len(space) - 1:
        num_adjacent_active += get_3d_adjacent_active(space[w + 1], level, row, col)
        if space[w + 1][level][row][col] == ACTIVE:
            num_adjacent_active += 1

    return num_adjacent_active

# Part 1
def simulate_3d(file_name, num_cycles):
    # Read the input into a 2D grid:
    input = []
    with open(file_name) as file:
        for line in file:
            line = line.strip()
            input.append(list(line))

    # Determine the maximum extents of the space after simulating the given number of cycles:
    depth_range = 1
    height_range = len(input)
    width_range = len(input[0])

    max_depth = depth_range + 2 * num_cycles
    max_height = height_range + 2 * num_cycles
    max_width = width_range + 2 * num_cycles

    # Initialize an empty space with those maximum extents:
    space = create_3d_space(max_width, max_height, max_depth)

    # Insert the input into the center of this larger space:
    for row in range(height_range):
        for col in range(width_range):
            space[0 + num_cycles][row + num_cycles][
                col + num_cycles
            ] = input[row][col]

    # For each cycle...
    cycle = 0
    while cycle < num_cycles:

        # ...save the current space:
        original_space = create_3d_space(max_width, max_height, max_depth)
        for level in range(max_depth):
            for row in range(max_height):
                for col in range(max_width):
                    original_space[level][row][col] = space[level][row][col]

        cycle += 1

        # ...update the space based on the simulation rules:
        for level in range(max_depth):
            for row in range(max_height):
                for col in range(max_width):
                    num_adjacent_active = get_3d_adjacent_active(
                        original_space, level, row, col
                    )

                    if (
                        space[level][row][col] == ACTIVE
                        and num_adjacent_active != 2
                        and num_adjacent_active != 3
                    ):
                        space[level][row][col] = INACTIVE
                    elif (
                        space[level][row][col] == INACTIVE
                        and num_adjacent_active == 3
                    ):
                        space[level][row][col] = ACTIVE

    # Return the number of active elements:
    num_active = 0

    for level in range(max_depth):
        for row in range(max_height):
            for col in range(max_width):
                if space[level][row][col] == ACTIVE:
                    num_active += 1

    return num_active

print(simulate_3d("input.txt", 6))

# Part 2
#
# Copied from the above, but with an added dimension; good enough.
def simulate_4d(file_name, num_cycles):
    # Read the input into a 2D grid:
    input = []
    with open(file_name) as file:
        for line in file:
            line = line.strip()
            input.append(list(line))

    # Determine the maximum extents of the space after simulating the given number of cycles:
    w_size_range = 1
    depth_range = 1
    height_range = len(input)
    width_range = len(input[0])

    max_w_size = w_size_range + 2 * num_cycles
    max_depth = depth_range + 2 * num_cycles
    max_height = height_range + 2 * num_cycles
    max_width = width_range + 2 * num_cycles

    # Initialize an empty space with those maximum extents:
    space = create_4d_space(max_width, max_height, max_depth, max_w_size)

    # Insert the input into the center of this larger space:
    for row in range(height_range):
        for col in range(width_range):
            space[0 + num_cycles][0 + num_cycles][row + num_cycles][
                col + num_cycles
            ] = input[row][col]

    # For each cycle...
    cycle = 0
    while cycle < num_cycles:

        # ...save the current space:
        original_space = create_4d_space(max_width, max_height, max_depth, max_w_size)
        for w_level in range(max_w_size):
            for level in range(max_depth):
                for row in range(max_height):
                    for col in range(max_width):
                        original_space[w_level][level][row][col] = space[w_level][
                            level
                        ][row][col]

        cycle += 1

        # ...update the space based on the simulation rules:
        for w_level in range(max_w_size):
            for level in range(max_depth):
                for row in range(max_height):
                    for col in range(max_width):
                        num_adjacent_active = get_4d_adjacent_active(
                            original_space, w_level, level, row, col
                        )

                        if (
                            space[w_level][level][row][col] == ACTIVE
                            and num_adjacent_active != 2
                            and num_adjacent_active != 3
                        ):
                            space[w_level][level][row][col] = INACTIVE
                        elif (
                            space[w_level][level][row][col] == INACTIVE
                            and num_adjacent_active == 3
                        ):
                            space[w_level][level][row][col] = ACTIVE

    # Return the number of active elements:
    num_active = 0

    for w_level in range(max_w_size):
        for level in range(max_depth):
            for row in range(max_height):
                for col in range(max_width):
                    if space[w_level][level][row][col] == ACTIVE:
                        num_active += 1

    return num_active

print(simulate_4d("input.txt", 6))
