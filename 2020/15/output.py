def update_history(history, number, turn):
    if number not in history:
        history[number] = []

    history[number].append(turn)

    if len(history[number]) > 2:
        history[number].pop(0)

    assert len(history[number]) <= 2

def get_last_number_called(file_name, turns_to_play):
    history = {}
    number = None
    turn = 1

    with open(file_name) as file:
        for number_string in file.read().splitlines()[0].split(","):
            number = int(number_string)
            update_history(history, number, turn)
            turn += 1

    for turn in range(turn, turns_to_play + 1):
        assert number in history

        number = (
            0 if len(history[number]) == 1 else history[number][1] - history[number][0]
        )
        update_history(history, number, turn)

    return number

# Part 1
print(get_last_number_called("input.txt", 2020))

# Part 2
print(get_last_number_called("input.txt", 30000000)) # (takes several seconds to run)
