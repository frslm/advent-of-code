class Operation:
    addition = 1
    multiplication = 2

def apply_operation(operation, lhs, rhs):
    if operation == Operation.addition:
        return lhs + rhs
    elif operation == Operation.multiplication:
        return lhs * rhs
    else:
        raise "Unknown operation!"

def add_parentheses_higher_addition_precedence(expression):
    index = 0
    while index < len(expression):
        # Look for the next '+', if any:
        addition_index = expression[index:].find("+")
        if addition_index == -1:
            break

        addition_index += index

        # Look for the end of the rhs and the start of the lhs:
        rhs_index = addition_index + 2
        depth = 0
        while True:
            if expression[rhs_index] == "(":
                depth += 1
            elif expression[rhs_index] == ")":
                depth -= 1

            if depth == 0:
                break

            rhs_index += 1

        lhs_index = addition_index - 2
        depth = 0
        while True:
            if expression[lhs_index] == ")":
                depth += 1
            elif expression[lhs_index] == "(":
                depth -= 1

            if depth == 0:
                break

            lhs_index -= 1

        # Wrap the lhs and rhs in parentheses if needed:
        index = addition_index + 1

        if (
            rhs_index + 1 > len(expression) - 1
            or expression[rhs_index + 1] != ")"
            or lhs_index - 1 < 0
            or expression[lhs_index - 1] != "("
        ):
            expression = expression[: rhs_index + 1] + ")" + expression[rhs_index + 1 :]
            expression = expression[:lhs_index] + "(" + expression[lhs_index:]
            index += 1  # (increment index since adding to the lhs pushes the index by a single step)

    return expression

def calculate_value_equal_precedence(expression):
    value = 0
    operation = Operation.addition

    index = 0
    while index < len(expression):
        if expression[index] == " ":
            pass

        elif expression[index] == "(":
            subexpression_value, subexpression_end = calculate_value_equal_precedence(
                expression[index + 1 :]
            )
            value = apply_operation(operation, value, subexpression_value)
            index += subexpression_end

        elif expression[index] == ")":
            return (value, index + 1)

        elif expression[index] == "+":
            operation = Operation.addition

        elif expression[index] == "*":
            operation = Operation.multiplication

        else:
            value = apply_operation(operation, value, int(expression[index]))

        index += 1

    return (value, index + 1)

# Part 1
def calculate_sum_of_all_expressions_equal_precedence(file_name):
    total_value = 0

    with open(file_name) as file:
        for line in file:
            total_value += calculate_value_equal_precedence(line.strip())[0]

    return total_value

print(calculate_sum_of_all_expressions_equal_precedence("input.txt"))

# Part 2
#
# Design note: To implement precedence, I opted to insert parentheses for the
# higher precedence operators first (as a preprocessing step) before solving
# the expression.
#
# An alternative might've been to tokenize everything into some tree perhaps?
def calculate_sum_of_all_expressions_higher_addition_precedence(file_name):
    total_value = 0

    with open(file_name) as file:
        for line in file:
            total_value += calculate_value_equal_precedence(
                add_parentheses_higher_addition_precedence(line.strip())
            )[0]

    return total_value

print(calculate_sum_of_all_expressions_higher_addition_precedence("input.txt"))
