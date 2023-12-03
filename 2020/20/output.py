from enum import Enum
import math

class Direction(Enum):
    top = 0
    right = 1
    bottom = 2
    left = 3

def create_2d_grid(width, height):
    return [[None for col in range(width)] for row in range(height)]

def flip(grid, axis_intersecting_direction):
    width = len(grid[0])
    height = len(grid)

    new_grid = create_2d_grid(width, height)

    # Flip horizontally:
    if axis_intersecting_direction in [Direction.top, Direction.bottom]:
        for row_index in range(height):
            for col_index in range(width):
                new_grid[row_index][width - 1 - col_index] = grid[row_index][col_index]

    # Flip vertically:
    elif axis_intersecting_direction in [Direction.left, Direction.right]:
        for row_index in range(height):
            for col_index in range(width):
                new_grid[height - 1 - row_index][col_index] = grid[row_index][col_index]

    return new_grid

def rotate_clockwise(grid):
    width = len(grid[0])
    height = len(grid)

    new_grid = create_2d_grid(height, width)

    for row_index in range(height):
        for col_index in range(width):
            new_grid[col_index][row_index] = grid[height - 1 - row_index][col_index]

    return new_grid

# Rotate the grid by `rotation` number of right angles clockwise.
def rotate(grid, rotation):
    new_grid = grid

    while rotation > 0:
        new_grid = rotate_clockwise(new_grid)
        rotation -= 1

    return new_grid

# Get the number of right angles clockwise needed to move from source to destination.
def get_rotation_required(source_direction, destination_direction):
    return (destination_direction.value - source_direction.value) % len(Direction)

# Get the opposite direction.
def get_opposite_direction(direction):
    HALF_ROTATION = len(Direction) // 2
    return Direction((direction.value + HALF_ROTATION) % len(Direction))

class EdgeSignature:
    def __init__(self, forward, reverse):
        self.forward = forward
        self.reverse = reverse

    @classmethod
    def from_edge_pixels(cls, edge_pixels):
        FILLED = "#"
        EMPTY = "."

        forward = 0
        reverse = 0

        index = 0
        last_index = len(edge_pixels) - 1
        while index <= last_index:
            forward <<= 1
            forward += 1 if edge_pixels[index] == FILLED else 0

            reverse <<= 1
            reverse += 1 if edge_pixels[last_index - index] == FILLED else 0

            index += 1

        return cls(forward, reverse)

    def flip(self):
        self.forward, self.reverse = self.reverse, self.forward

class Tile:
    def __init__(self, number, grid):
        self.number = number
        self.grid = grid

        self.edges = {
            Direction.top: EdgeSignature.from_edge_pixels(
                [self.grid[0][index] for index in range(len(self.grid[0]))]
            ),
            Direction.right: EdgeSignature.from_edge_pixels(
                [
                    self.grid[index][len(self.grid[0]) - 1]
                    for index in range(len(self.grid))
                ]
            ),
            Direction.bottom: EdgeSignature.from_edge_pixels(
                [
                    self.grid[len(self.grid) - 1][index]
                    for index in reversed(range(len(self.grid[0])))
                ]
            ),
            Direction.left: EdgeSignature.from_edge_pixels(
                [self.grid[index][0] for index in reversed(range(len(self.grid)))]
            ),
        }
        # (signature of each edge going clockwise, where the signature is the binary value of the edge where '.' are 0s and '#' are 1s)

        self.row = None
        self.col = None

    def flip(self, axis_intersecting_direction):
        # Flip all edges:
        for direction in Direction:
            self.edges[direction].flip()

        # And swap non-axis-intersecting edges:
        if axis_intersecting_direction in [Direction.top, Direction.bottom]:
            self.edges[Direction.left], self.edges[Direction.right] = (
                self.edges[Direction.right],
                self.edges[Direction.left],
            )

        elif axis_intersecting_direction in [Direction.left, Direction.right]:
            self.edges[Direction.top], self.edges[Direction.bottom] = (
                self.edges[Direction.bottom],
                self.edges[Direction.top],
            )

        # Flip the actual grid:
        self.grid = flip(self.grid, axis_intersecting_direction)

    # Rotate the tile by `rotation` number of right angles clockwise.
    def rotate(self, rotation):
        # Rotate the grid edges:
        (
            self.edges[Direction((Direction.top.value + rotation) % len(Direction))],
            self.edges[Direction((Direction.right.value + rotation) % len(Direction))],
            self.edges[Direction((Direction.bottom.value + rotation) % len(Direction))],
            self.edges[Direction((Direction.left.value + rotation) % len(Direction))],
        ) = (
            self.edges[Direction.top],
            self.edges[Direction.right],
            self.edges[Direction.bottom],
            self.edges[Direction.left],
        )

        # Rotate the actual grid:
        self.grid = rotate(self.grid, rotation)

def read_input(file_name):
    TILE_PREFIX = "Tile "

    with open(file_name) as file:
        tiles = {}

        for line in file:
            line = line.strip()

            if line[: len(TILE_PREFIX)] == TILE_PREFIX:
                tile_number = int(line[len(TILE_PREFIX) : -1])
                grid = []

            elif line:
                grid.append(list(line))

            else:
                tiles[tile_number] = Tile(tile_number, grid)

    return tiles

def get_adjacent_row_col(tile, direction):
    row = tile.row
    col = tile.col

    if direction == Direction.top:
        row -= 1
    elif direction == Direction.right:
        col += 1
    elif direction == Direction.bottom:
        row += 1
    elif direction == Direction.left:
        col -= 1
    else:
        raise "Unknown direction!"

    return (row, col)

def piece_together_tiles(tiles):
    # Start off with a complete list of all tiles:
    remaining_tiles = [tile for number, tile in tiles.items()]
    unexplored_edges = {}

    # Knowing that the image and each tile are squares, create a square space
    # large enough to contain the image (starting from a single tile, space
    # should be large enough to accomodate `num_tiles_per_side` tiles in any
    # direction):
    num_tiles_per_side = int(math.sqrt(len(remaining_tiles)))
    output_space_tiles_per_side = 2 * num_tiles_per_side - 1
    tile_grid = create_2d_grid(output_space_tiles_per_side, output_space_tiles_per_side)

    # Pick a starting tile and place it in the center:
    starting_tile = remaining_tiles.pop()
    starting_tile.row = starting_tile.col = output_space_tiles_per_side // 2
    tile_grid[starting_tile.row][starting_tile.col] = starting_tile

    # Then add its edges to the dict of unexplored edges:
    for direction in Direction:
        unexplored_edges[starting_tile.edges[direction].forward] = (
            starting_tile.number,
            direction,
        )

    # While there exist some unexplored edges...
    while unexplored_edges:

        # ...pick one of those edges:
        (
            unexplored_edge_forward_signature,
            (source_tile_number, unexplored_edge_direction),
        ) = unexplored_edges.popitem()

        # ...then for each of the remaining tiles...
        for index, tile in enumerate(remaining_tiles):

            # ...try to find a matching edge in one of the remaining tiles:
            matching_edge_direction = None

            for direction in Direction:
                if unexplored_edge_forward_signature in [
                    tile.edges[direction].forward,
                    tile.edges[direction].reverse,
                ]:
                    matching_edge_direction = direction
                    break

            # ...if a matching edge is found...
            if matching_edge_direction:

                # ...and it's another forward edge, flip the whole tile to get a matching reverse edge:
                if (
                    unexplored_edge_forward_signature
                    == tile.edges[matching_edge_direction].forward
                ):
                    tile.flip(matching_edge_direction)

                # ...rotate the tile so that the matching reverse edge is opposite the unexplored edge:
                required_opposite_edge_direction = get_opposite_direction(
                    unexplored_edge_direction
                )
                tile.rotate(
                    get_rotation_required(
                        matching_edge_direction, required_opposite_edge_direction
                    )
                )

                # ...place the tile adjacent to the source tile on the grid:
                tile.row, tile.col = get_adjacent_row_col(
                    tiles[source_tile_number], unexplored_edge_direction
                )
                tile_grid[tile.row][tile.col] = tile

                # ...add the other edges of the new tile:
                for direction in Direction:
                    if direction != required_opposite_edge_direction:
                        unexplored_edges[tile.edges[direction].forward] = (
                            tile.number,
                            direction,
                        )

                # ...remove this tile from the list of remaining tiles:
                remaining_tiles.pop(index)

                # ...break out now that this tile has been matched:
                break

    return tile_grid

def move_to_edge(tile, tile_grid, direction):
    while True:
        next_row, next_col = get_adjacent_row_col(tile, direction)

        # If out-of-bounds or if there's no adjacent tile, break:
        if (
            next_row >= 0
            and next_row <= len(tile_grid) - 1
            and next_col >= 0
            and next_col <= len(tile_grid[0]) - 1
        ):
            adjacent_tile = tile_grid[next_row][next_col]
            if adjacent_tile is not None:
                tile = adjacent_tile
            else:
                break
        else:
            break

    return tile

def get_ids_of_corner_tiles(connected_tiles, tile_grid):
    corner_tile_ids = []

    # Start off at any tile:
    tile = next(iter(connected_tiles.values()))

    # Top-left:
    tile = move_to_edge(tile, tile_grid, Direction.left)
    tile = move_to_edge(tile, tile_grid, Direction.top)
    corner_tile_ids.append(tile.number)

    # Top-right:
    tile = move_to_edge(tile, tile_grid, Direction.right)
    corner_tile_ids.append(tile.number)

    # Bottom-right:
    tile = move_to_edge(tile, tile_grid, Direction.bottom)
    corner_tile_ids.append(tile.number)

    # Bottom-left:
    tile = move_to_edge(tile, tile_grid, Direction.left)
    corner_tile_ids.append(tile.number)

    return corner_tile_ids

def stitch_tiles(connected_tiles, tile_grid):
    # Start off at the top-left tile:
    tile = next(iter(connected_tiles.values()))
    tile = move_to_edge(tile, tile_grid, Direction.left)
    tile = move_to_edge(tile, tile_grid, Direction.top)
    row, col = tile.row, tile.col

    # Get the number of pixels per tile and the number of tiles per grid side,
    # then create a blank grid to hold the final stitched image:
    num_pixels_per_tile = (
        len(tile.grid) - 2
    )  # (subtract the borders since they won't be included)
    num_tiles_per_side = (
        len(tile_grid) + 1
    ) // 2  # (this is the reverse of the operation used in `piece_together_tiles()`)
    stitched_image = create_2d_grid(
        num_pixels_per_tile * num_tiles_per_side,
        num_pixels_per_tile * num_tiles_per_side,
    )

    # For each tile in the tile grid...
    for tile_row_index in range(num_tiles_per_side):
        for tile_col_index in range(num_tiles_per_side):

            # ...for each pixel in the tile...
            for pixel_row_index in range(num_pixels_per_tile):
                for pixel_col_index in range(num_pixels_per_tile):

                    # ...copy the non-border portion to the final stitched image:
                    stitched_image[
                        tile_row_index * num_pixels_per_tile + pixel_row_index
                    ][
                        tile_col_index * num_pixels_per_tile + pixel_col_index
                    ] = tile_grid[
                        row + tile_row_index
                    ][
                        col + tile_col_index
                    ].grid[
                        pixel_row_index + 1
                    ][
                        pixel_col_index + 1
                    ]

    return stitched_image

def get_pixel_locations(image, pixel_of_interest):
    pixel_locations = []

    for row_index, row in enumerate(image):
        for col_index, pixel in enumerate(row):
            if pixel == pixel_of_interest:
                pixel_locations.append((row_index, col_index))

    return pixel_locations

def mark_entity(image, entity, filled_pixel, pixel_to_mark_with):
    image_height = len(image)
    image_width = len(image[0])

    NUM_RIGHT_ANGLE_ROTATIONS = 4
    NUM_MIRROR_FLIPS = 2

    # For every possible mirror-flip orientation...
    for _ in range(NUM_MIRROR_FLIPS):
        entity = flip(entity, Direction.top)

        # ...for every possibly right-angle rotation...
        for _ in range(NUM_RIGHT_ANGLE_ROTATIONS):
            entity = rotate(entity, 1)

            # ...get the dimensions of the entity:
            entity_height = len(entity)
            entity_width = len(entity[0])

            # ...get the locations of all filled pixels in the entity:
            entity_pixel_locations = get_pixel_locations(entity, filled_pixel)

            # ...for every subsection of the image that can fit the entity...
            for row in range(image_height - entity_height + 1):
                for col in range(image_width - entity_width + 1):

                    # ...look for the entity:
                    entity_found = True
                    for pixel_location in entity_pixel_locations:
                        if (
                            image[row + pixel_location[0]][col + pixel_location[1]]
                            != filled_pixel
                        ):
                            entity_found = False
                            break

                    # ...if the entity has been found, mark all the pixels of
                    # the image that make up the entity:
                    if entity_found:
                        for pixel_location in entity_pixel_locations:
                            image[row + pixel_location[0]][
                                col + pixel_location[1]
                            ] = pixel_to_mark_with

def count_pixel(stitched_image, pixel_of_interest):
    num_pixel = 0

    for row in stitched_image:
        for pixel in row:
            if pixel == pixel_of_interest:
                num_pixel += 1

    return num_pixel

# Part 1
tiles = read_input("input.txt")
tile_grid = piece_together_tiles(tiles)
corner_ids_multiplied = 1
for ids_of_corner_tile in get_ids_of_corner_tiles(tiles, tile_grid):
    corner_ids_multiplied *= ids_of_corner_tile
print(corner_ids_multiplied)

# Part 2
SEA_MONSTER = [
    list("                  # "),
    list("#    ##    ##    ###"),
    list(" #  #  #  #  #  #   "),
]
SEA_MONSTER_FILLED_PIXEL = "#"
stitched_image = stitch_tiles(tiles, tile_grid)
mark_entity(stitched_image, SEA_MONSTER, SEA_MONSTER_FILLED_PIXEL, "O")
print(count_pixel(stitched_image, "#"))
