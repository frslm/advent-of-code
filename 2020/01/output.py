# Part 1
def find_product_of_two(target_sum, file_name):
    previously_seen_entries = set()

    with open(file_name) as input:
        for line in input:
            entry = int(line)
            complement = target_sum - entry
            if complement in previously_seen_entries:
                return entry * complement
            else:
                previously_seen_entries.add(entry)

    return None

print(find_product_of_two(2020, "input.txt"))

# Part 2
def find_product_of_three(target_sum, file_name):
    # Note: Can this be improved? It's O(N^2).
    with open(file_name) as input:
        for line in input:
            entry = int(line)
            product_of_two = find_product_of_two(target_sum - entry, file_name)
            # Note: Can avoid opening the file again and again.
            if product_of_two is not None:
                return entry * product_of_two

    return None

print(find_product_of_three(2020, "input.txt"))
