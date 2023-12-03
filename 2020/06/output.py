# Part 1
def get_num_any_answers(file_name):
    num_answers = 0
    answers = set()

    with open(file_name) as input:
        for line in input:
            if line != "\n":
                for answer in line.strip():
                    answers.add(answer)
            else:
                num_answers += len(answers)
                answers.clear()

    if answers:
        num_answers += len(answers)
        answers.clear()

    return num_answers

print(get_num_any_answers("input.txt"))

# Part 2
def get_num_common_answers(file_name):
    num_answers = 0
    answers = set()
    first_member = True

    with open(file_name) as input:
        for line in input:
            if line != "\n":
                line = line.strip()
                if first_member:
                    for answer in line:
                        answers.add(answer)
                    first_member = False
                else:
                    to_remove = set()

                    for answer in answers:
                        if answer not in line:
                            to_remove.add(answer)

                    for answer in to_remove:
                        answers.remove(answer)

            else:
                num_answers += len(answers)
                answers.clear()
                first_member = True

    if answers:
        num_answers += len(answers)
        answers.clear()

    return num_answers

print(get_num_common_answers("input.txt"))
