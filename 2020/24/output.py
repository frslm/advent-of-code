NORTH = "n"
SOUTH = "s"
EAST = "e"
WEST = "w"

NORTH_EAST = "ne"
NORTH_WEST = "nw"
SOUTH_EAST = "se"
SOUTH_WEST = "sw"

class HexPoint:
    def __init__(self, w_e=0, nw_se=0, ne_sw=0):
        self.w_e = w_e  # (east is positive)
        self.nw_se = nw_se  # (north-west is positive)
        self.ne_sw = ne_sw  # (south-west is positive)

    @classmethod
    def copy(cls, other):
        return cls(other.w_e, other.nw_se, other.ne_sw)

    def __eq__(self, other):
        return (
            isinstance(other, HexPoint)
            and self.w_e == other.w_e
            and self.nw_se == other.nw_se
            and self.ne_sw == other.ne_sw
        )

    def __hash__(self):
        return hash((self.w_e, self.nw_se, self.ne_sw))

    # Straighten out any kinks, minimizing the total number of moves needed to reach this point.
    def delete_kinks(self):
        while True:
            # If kink-forming coordinates have the same sign, get their common distance:
            if self.w_e > 0 and self.nw_se > 0:
                common_distance = min(self.w_e, self.nw_se)
            elif self.w_e < 0 and self.nw_se < 0:
                common_distance = max(self.w_e, self.nw_se)
            elif self.w_e > 0 and self.ne_sw > 0:
                common_distance = min(self.w_e, self.ne_sw)
            elif self.w_e < 0 and self.ne_sw < 0:
                common_distance = max(self.w_e, self.ne_sw)
            elif self.ne_sw > 0 and self.nw_se > 0:
                common_distance = min(self.ne_sw, self.nw_se)
            elif self.ne_sw < 0 and self.nw_se < 0:
                common_distance = max(self.ne_sw, self.nw_se)
            else:
                break  # (no more kinks)

            # Subtract their common distance from each:
            self.w_e -= common_distance
            self.nw_se -= common_distance
            self.ne_sw -= common_distance

    def shift(self, direction, distance):
        if direction == WEST:
            self.w_e -= distance
        elif direction == EAST:
            self.w_e += distance
        elif direction == SOUTH_EAST:
            self.nw_se -= distance
        elif direction == NORTH_WEST:
            self.nw_se += distance
        elif direction == NORTH_EAST:
            self.ne_sw -= distance
        elif direction == SOUTH_WEST:
            self.ne_sw += distance
        else:
            raise "Unknown direction!"

        self.delete_kinks()

class HexGrid:
    def __init__(self):
        self.black_tiles = set()

    def flip_tile(self, hex_point):
        # If the hex point already exists in the set of tiles, remove it (flip from black to white):
        for black_tile in self.black_tiles:
            if black_tile == hex_point:
                self.black_tiles.remove(black_tile)
                return

        # Otherwise, add it (flip from white to black):
        self.black_tiles.add(hex_point)

def flip_tiles(file_name):
    grid = HexGrid()

    with open(file_name) as file:
        for line in file:
            point = HexPoint()

            for char in line.strip():
                if char == NORTH or char == SOUTH:
                    verticality = char

                elif char == EAST or char == WEST:
                    if char == EAST:
                        if verticality == NORTH:
                            point.ne_sw -= 1
                        elif verticality == SOUTH:
                            point.nw_se -= 1
                        else:
                            point.w_e += 1
                    else:
                        if verticality == NORTH:
                            point.nw_se += 1
                        elif verticality == SOUTH:
                            point.ne_sw += 1
                        else:
                            point.w_e -= 1

                    verticality = None

                else:
                    raise "Unknown direction character!"

            point.delete_kinks()
            grid.flip_tile(point)

    return grid

HEX_DIRECTIONS = [WEST, EAST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST]

def get_adjacent_white_tiles(grid, tile):
    white_tiles = []

    for direction in HEX_DIRECTIONS:
        new_tile = HexPoint.copy(tile)
        new_tile.shift(direction, 1)
        if new_tile not in grid.black_tiles:
            white_tiles.append(new_tile)

    return white_tiles

def exactly_n_adjacent_black_tiles(grid, tile, n):
    num_adjacent_black_tiles = 0

    for direction in HEX_DIRECTIONS:
        new_tile = HexPoint.copy(tile)
        new_tile.shift(direction, 1)
        if new_tile in grid.black_tiles:
            num_adjacent_black_tiles += 1

        if num_adjacent_black_tiles > n:
            break

    return num_adjacent_black_tiles == n

def simulate_day(grid):
    new_grid = HexGrid()

    # For each black tile in the old grid...
    for black_tile in grid.black_tiles:

        # ...get surrounding white tiles:
        white_tiles = get_adjacent_white_tiles(grid, black_tile)

        # ...if there are exactly 4 or 5 adjacent white tiles, then add this black tile to the new grid:
        if len(white_tiles) in [4, 5]:
            new_grid.black_tiles.add(black_tile)

        # ...for each surrounding white tile with exactly 2 black tiles, add this white tile to new grid (as a black tile):
        for white_tile in white_tiles:
            if exactly_n_adjacent_black_tiles(grid, white_tile, 2):
                new_grid.black_tiles.add(white_tile)

    return new_grid

def simulate_days(grid, num_days):
    while num_days > 0:
        num_days -= 1
        grid = simulate_day(grid)

    return grid

# Part 1
grid = flip_tiles("input.txt")
print(len(grid.black_tiles))

# Part 2
NUM_DAYS = 100
grid = simulate_days(grid, NUM_DAYS)
print(len(grid.black_tiles))
