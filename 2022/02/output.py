# Part 1
symbol_scores = {
    "X": 1,  # rock
    "Y": 2,  # paper
    "Z": 3,  # scissors
}

equal = {
    "A": "X",  # rock
    "B": "Y",  # paper
    "C": "Z",  # scissors
}

beaten_by = {
    "A": "Y",  # rock beaten by paper
    "B": "Z",  # paper beaten by scissors
    "C": "X",  # scissors beaten by rock
}

beats = {
    "A": "Z",  # rock beats scissors
    "B": "X",  # paper beats rock
    "C": "Y",  # scissors beats paper
}

total_score = 0

with open("input.txt") as input:
    for line in input:
        line = line.strip()

        round_score = 0

        opponent = line[0]  # A,B,C
        mine = line[-1]  # X,Y,Z

        round_score += symbol_scores[mine]

        if beaten_by[opponent] == mine:
            round_score += 6
        elif equal[opponent] == mine:
            round_score += 3

        total_score += round_score


print(total_score)

# Part 2
total_score = 0

with open("input.txt") as input:
    for line in input:
        line = line.strip()

        round_score = 0

        opponent = line[0]  # A,B,C
        outcome = line[-1]  # X=lose,Y=draw,Z=win

        if outcome == "X":
            mine = beats[opponent]
        elif outcome == "Y":
            round_score += 3
            mine = equal[opponent]
        else:
            assert outcome == "Z"
            round_score += 6
            mine = beaten_by[opponent]

        round_score += symbol_scores[mine]

        total_score += round_score

print(total_score)
