class Result:
    def __init__(self, acc=0, infinite_loop=False):
        self.acc = acc
        self.infinite_loop = infinite_loop
        self.visited_indices = set()

def _extract_instructions(file_name):
    with open(file_name) as file:
        return file.read().splitlines()

def _get_instruction_code(instruction_string):
    return instruction_string.split()[0]

def _get_instruction_value(instruction_string):
    value_string = instruction_string.split()[1]
    return (1 if value_string[0] == "+" else -1) * int(value_string[1:])

def _run_instructions(instructions):
    result = Result()
    index = 0

    while index < len(instructions):
        if index in result.visited_indices:
            result.infinite_loop = True
            break

        code = _get_instruction_code(instructions[index])
        value = _get_instruction_value(instructions[index])

        result.visited_indices.add(index)

        if code == "nop":
            index += 1

        elif code == "acc":
            result.acc += value
            index += 1

        elif code == "jmp":
            index += value

    return result

# Part 1
def find_acc_before_infinite_loop(file_name):
    return _run_instructions(_extract_instructions(file_name)).acc

print(find_acc_before_infinite_loop("input.txt"))

# Part 2
#
# Find all visited instructions in the buggy opcode, then change each one (if
# 'nop' or 'jmp') until a terminating program is found, then return its `acc`
# value.
def find_acc_after_fix(file_name):
    original_instructions = _extract_instructions(file_name)
    original_result = _run_instructions(original_instructions)
    assert original_result.infinite_loop

    for visited_index in original_result.visited_indices:
        instruction = original_instructions[visited_index]
        code = _get_instruction_code(instruction)

        if code == "nop" or code == "jmp":
            new_instructions = list(original_instructions)
            new_instructions[visited_index] = instruction.replace(
                code, "jmp" if code == "nop" else "nop"
            )
            new_result = _run_instructions(new_instructions)
            if not new_result.infinite_loop:
                return new_result.acc


print(find_acc_after_fix("input.txt"))
