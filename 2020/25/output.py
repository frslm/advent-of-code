def read_input(file_name):
    with open(file_name) as file:
        lines = file.read().splitlines()
        return int(lines[0]), int(lines[1])

def transform_subject_number_once(value, subject_number):
    value *= subject_number
    value %= 20201227
    return value

def transform_subject_number(subject_number, loop_size):
    value = 1
    while loop_size > 0:
        value = transform_subject_number_once(value, subject_number)
        loop_size -= 1

    return value

def brute_force_loop_size(subject_number, target_value):
    loop_size = 0

    value = 1
    while value != target_value:
        value = transform_subject_number_once(value, subject_number)
        loop_size += 1

    return loop_size

# Part 1
card_public_key, door_public_key = read_input("input.txt")

card_loop_size = brute_force_loop_size(7, card_public_key)
door_loop_size = brute_force_loop_size(7, door_public_key)

card_encryption_key = transform_subject_number(door_public_key, card_loop_size)
door_encryption_key = transform_subject_number(card_public_key, door_loop_size)
assert card_encryption_key == door_encryption_key

print(card_encryption_key)

# Part 2
# (nothing else to do!)
