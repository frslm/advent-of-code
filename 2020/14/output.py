MASK = "mask"
MEMORY = "mem"

# Part 1
def build_mask(mask_string):
    mask = {}

    for index, char in enumerate(reversed(mask_string)):
        if char != "X":
            mask[index] = int(char)

    return mask

def get_masked_value(mask, value):
    for index, bit in mask.items():
        if ((value >> index) & 1) ^ bit:
            modification = 2 ** index

            if bit == 0:
                modification *= -1

            value += modification

    return value

def remaining_memory_sum(file_name):
    memory = {}

    with open(file_name) as file:
        for line in file:
            line = line.strip()

            if line[: len(MASK)] == MASK:
                mask = build_mask(line.split(" = ")[1])

            elif line[: len(MEMORY)] == MEMORY:
                split = line.split(" = ")
                address = int(split[0][len(MEMORY + "[") : -len("]")])
                value = int(split[1])

                memory[address] = get_masked_value(mask, value)

            else:
                raise "Unknown command!"

    return sum([value for address, value in memory.items()])

print(remaining_memory_sum("input.txt"))

# Part 2
def get_writeable_memory_addresses(mask_string, address):
    template = []
    for index, char in enumerate(reversed(mask_string)):
        if char == "0":
            template.insert(0, str((address >> index) & 1))
        else:
            template.insert(0, char)

    addresses = [template]
    while True:
        if "X" in addresses[0]:
            index = addresses[0].index("X")

            zeroed = list(addresses[0])
            zeroed[index] = "0"
            addresses.append(zeroed)

            oned = list(addresses[0])
            oned[index] = "1"
            addresses.append(oned)

            addresses.pop(0)
        else:
            break

    return [int("".join(address), 2) for address in addresses]

def remaining_memory_sum_v2(file_name):
    memory = {}

    with open(file_name) as file:
        for line in file:
            line = line.strip()

            if line[: len(MASK)] == MASK:
                mask = line.split(" = ")[1]

            elif line[: len(MEMORY)] == MEMORY:
                split = line.split(" = ")
                address = int(split[0][len(MEMORY + "[") : -len("]")])
                value = int(split[1])

                for address in get_writeable_memory_addresses(mask, address):
                    memory[address] = value

            else:
                raise "Unknown command!"

    return sum([value for address, value in memory.items()])

print(remaining_memory_sum_v2("input.txt"))
