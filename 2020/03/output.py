def count_slope_trees(file_name, speed):
    TREE = "#"

    num_trees = 0

    coord_right = 0
    coord_down = 0

    with open(file_name) as input:
        grid = input.read().splitlines()
        while coord_down + speed[1] < len(grid) - 1:
            coord_right = (coord_right + speed[0]) % len(grid[0])
            coord_down += speed[1]

            if grid[coord_down][coord_right] == TREE:
                num_trees += 1

    return num_trees

# Part 1
print(count_slope_trees("input.txt", (3, 1)))

# Part 2
multiplied_slopes = 1
for speed in [(1, 1), (3, 1), (5, 1), (7, 1), (1, 2)]:
    multiplied_slopes *= count_slope_trees("input.txt", speed)

print(multiplied_slopes)
