class ScanningMode:
    player_one = 1
    player_two = 2

def read_input(file_name):
    player_one = []
    player_two = []

    scanning_mode = None
    with open(file_name) as file:
        for line in file:
            line = line.strip()

            if line == "Player 1:":
                scanning_mode = ScanningMode.player_one
            elif line == "Player 2:":
                scanning_mode = ScanningMode.player_two
            elif line:
                if scanning_mode == ScanningMode.player_one:
                    player_one.append(int(line))
                elif scanning_mode == ScanningMode.player_two:
                    player_two.append(int(line))
                else:
                    raise "Unknown scanning mode!"

    return player_one, player_two

def give_cards_to_round_winner(
    player_one_is_winner, player_one_card, player_two_card, player_one, player_two
):
    if player_one_is_winner:
        player_one.append(player_one_card)
        player_one.append(player_two_card)
    else:
        player_two.append(player_two_card)
        player_two.append(player_one_card)

def play_round(player_one, player_two):
    player_one_card = player_one.pop(0)
    player_two_card = player_two.pop(0)

    give_cards_to_round_winner(
        player_one_card > player_two_card,
        player_one_card,
        player_two_card,
        player_one,
        player_two,
    )

def play_till_end(player_one, player_two):
    while player_one and player_two:
        play_round(player_one, player_two)

def play_recursive_round(player_one, player_two):
    player_one_card = player_one.pop(0)
    player_two_card = player_two.pop(0)

    # If there are enough cards to play a recursive game, then do so:
    if len(player_one) >= player_one_card and len(player_two) >= player_two_card:
        player_one_is_winner = play_recursive_till_end(
            list(player_one[:player_one_card]), list(player_two[:player_two_card])
        )

    # Otherwise, just play a regular round:
    else:
        player_one_is_winner = player_one_card > player_two_card

    give_cards_to_round_winner(
        player_one_is_winner, player_one_card, player_two_card, player_one, player_two
    )

def history_repeated(history, player_one, player_two):
    for state in history:
        if state[0] == player_one and state[1] == player_two:
            return True

    return False

# Returns True is player one won, False otherwise.
def play_recursive_till_end(player_one, player_two):
    history = []

    while player_one and player_two:

        # Check if this state has been repeated in history:
        if history_repeated(history, player_one, player_two):
            return True

        # Add this round to history:
        history.append((list(player_one), list(player_two)))

        # Play the next round:
        play_recursive_round(player_one, player_two)

    return bool(player_one)

def calculate_score(deck):
    score = 0
    for index, card in enumerate(reversed(deck)):
        score += (index + 1) * card

    return score

# Part 1
player_one, player_two = read_input("input.txt")
play_till_end(player_one, player_two)
winner = player_one if player_one else player_two
print(calculate_score(winner))

# Part 2
player_one, player_two = read_input("input.txt")
player_one_is_winner = play_recursive_till_end(player_one, player_two)
winner = player_one if player_one_is_winner else player_two
print(calculate_score(winner))
