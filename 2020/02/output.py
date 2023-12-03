# Part 1
def get_num_valid_passwords(file_name):
    num_valid_passwords = 0

    with open(file_name) as input:
        for line in input:
            split = line.split(": ")
            password = split[1]
            split = split[0].split(" ")
            letter = split[1]
            split = split[0].split("-")
            low = int(split[0])
            high = int(split[1])

            occurences = 0
            for c in password:
                if c == letter:
                    occurences += 1

            if occurences >= low and occurences <= high:
                num_valid_passwords += 1

    return num_valid_passwords

print(get_num_valid_passwords("input.txt"))

# Part 2
def get_num_actually_valid_passwords(file_name):
    num_valid_passwords = 0

    with open(file_name) as input:
        for line in input:
            split = line.split(": ")
            password = split[1]
            split = split[0].split(" ")
            letter = split[1]
            split = split[0].split("-")
            first = int(split[0]) - 1  # (changes from 1-based to 0-based indexing)
            second = int(split[1]) - 1  # (changes from 1-based to 0-based indexing)

            occurences = 0
            if password[first] == letter:
                occurences += 1

            if password[second] == letter:
                occurences += 1

            if occurences == 1:  # (must have exactly one occurence to be valid)
                num_valid_passwords += 1

    return num_valid_passwords

print(get_num_actually_valid_passwords("input.txt"))
