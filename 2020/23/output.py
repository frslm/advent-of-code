def read_input(file_name):
    with open(file_name) as file:
        return [int(cup) for cup in list(next(iter(file)).strip())]

class Cup:
    def __init__(self, value):
        self.value = value
        self.next_cup = None

# Efficient linked-list with dict indexing implementation.
def play_rounds(input_cups, num_rounds, num_cups_to_pick_up, total_num_cups):
    cup_dict = {}

    # Add the input cups to a linked list:
    first_cup = None
    current_cup = None
    for cup in input_cups:
        if current_cup is None:
            current_cup = Cup(cup)
            first_cup = current_cup
        else:
            current_cup.next_cup = Cup(cup)
            current_cup = current_cup.next_cup

        cup_dict[current_cup.value] = current_cup

    # Append the remaining cups to the linked list:
    for cup in range(total_num_cups - len(input_cups)):
        current_cup.next_cup = Cup(cup + len(input_cups) + 1)
        current_cup = current_cup.next_cup
        cup_dict[current_cup.value] = current_cup

    # Join the end of the linked list to the start to form a loop:
    current_cup.next_cup = first_cup

    # While there still are rounds to play...
    current_cup = first_cup
    while num_rounds > 0:
        num_rounds -= 1

        # ...pick up cups to the right of the current cup:
        first_picked_up_cup = current_cup.next_cup
        last_picked_up_cup = first_picked_up_cup
        picked_up_cup_values = {last_picked_up_cup.value}
        for _ in range(num_cups_to_pick_up - 1):
            last_picked_up_cup = last_picked_up_cup.next_cup
            picked_up_cup_values.add(last_picked_up_cup.value)
        current_cup.next_cup = last_picked_up_cup.next_cup

        # ...find the destination cup:
        destination_cup_value = current_cup.value
        while True:
            destination_cup_value -= 1
            if destination_cup_value < 1:
                destination_cup_value = total_num_cups

            if destination_cup_value not in picked_up_cup_values:
                destination_cup = cup_dict[destination_cup_value]
                break

        # ...insert the picked-up cups to the right of the destination cup:
        last_picked_up_cup.next_cup = destination_cup.next_cup
        destination_cup.next_cup = first_picked_up_cup

        # ...move to the next cup:
        current_cup = current_cup.next_cup

    return cup_dict

# Part 1
TOTAL_NUM_CUPS = 9
NUM_MOVES = 100
NUM_CUPS_TO_PICK_UP = 3
cup_dict = play_rounds(
    read_input("input.txt"), NUM_MOVES, NUM_CUPS_TO_PICK_UP, TOTAL_NUM_CUPS
)

cup = cup_dict[1]
cup_values = []
for _ in range(TOTAL_NUM_CUPS - 1):
    cup = cup.next_cup
    cup_values.append(cup.value)
print("".join([str(cup) for cup in cup_values]))

# Part 2
TOTAL_NUM_CUPS = 1000000
NUM_MOVES = 10000000
NUM_CUPS_TO_RIGHT_TO_CHECK = 2
cup_dict = play_rounds(
    read_input("input.txt"), NUM_MOVES, NUM_CUPS_TO_PICK_UP, TOTAL_NUM_CUPS
)

cup = cup_dict[1]
answer = 1
while NUM_CUPS_TO_RIGHT_TO_CHECK > 0:
    NUM_CUPS_TO_RIGHT_TO_CHECK -= 1
    cup = cup.next_cup
    answer *= cup.value

print(answer)
