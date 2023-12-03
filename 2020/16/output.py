from functools import reduce

class ScanningMode:
    rules = 1
    personal_ticket = 2
    nearby_tickets = 3

class Rule:
    def __init__(self, name, ranges=None):
        self.name = name
        self.ranges = [] if ranges is None else ranges

def _read_rules_and_tickets(file_name):
    rules = []
    personal_ticket = None
    nearby_tickets = []

    parse_ticket = lambda string: [
        int(number_string) for number_string in string.split(",")
    ]

    scanning_mode = ScanningMode.rules
    with open(file_name) as file:
        for line in file:
            line = line.strip()

            if scanning_mode == ScanningMode.rules:
                if line:
                    split = line.split(": ")
                    name = split[0]
                    rule = Rule(name)

                    range_strings = split[1].split(" or ")
                    assert len(range_strings) == 2

                    for range_string in range_strings:
                        range_list = range_string.split("-")
                        assert len(range_list) == 2
                        rule.ranges.append((int(range_list[0]), int(range_list[1])))

                    rules.append(rule)
                else:
                    scanning_mode = ScanningMode.personal_ticket

            elif scanning_mode == ScanningMode.personal_ticket:
                if line:
                    if line == "your ticket:":
                        pass
                    else:
                        personal_ticket = parse_ticket(line)
                else:
                    scanning_mode = ScanningMode.nearby_tickets

            elif scanning_mode == ScanningMode.nearby_tickets:
                if line:
                    if line == "nearby tickets:":
                        pass
                    else:
                        nearby_tickets.append(parse_ticket(line))
                else:
                    scanning_mode = None

            else:
                raise "Unknown scanning mode!"

    return (rules, personal_ticket, nearby_tickets)

def _get_invalid_tickets(rules, nearby_tickets):
    invalid_ticket_numbers = []
    invalid_values = []

    for ticket_number, ticket in enumerate(nearby_tickets):
        valid_ticket = True

        for value in ticket:
            valid = False

            for rule in rules:
                for range_tuple in rule.ranges:
                    if value >= range_tuple[0] and value <= range_tuple[1]:
                        valid = True
                        break

                if valid:
                    break

            if not valid:
                valid_ticket = False
                invalid_values.append(value)

        if not valid_ticket:
            invalid_ticket_numbers.append(ticket_number)

    return (invalid_ticket_numbers, invalid_values)

# Part 1
def get_nearby_ticket_error_scanning_rate(file_name):
    rules, _, nearby_tickets = _read_rules_and_tickets(file_name)
    _, invalid_values = _get_invalid_tickets(rules, nearby_tickets)
    return sum(invalid_values)

print(get_nearby_ticket_error_scanning_rate("input.txt"))

# Part 2
def _get_field_rule_map(rules, personal_ticket, nearby_tickets):
    invalid_ticket_numbers, _ = _get_invalid_tickets(rules, nearby_tickets)

    # Get the valid tickets only:
    for ticket_number in reversed(invalid_ticket_numbers):
        nearby_tickets.pop(ticket_number)

    valid_tickets = [personal_ticket] + nearby_tickets

    # Figure out how many rules each field (of every valid ticket) abides by:
    valid_rule_numbers_per_field = {}  # (key: field, value: set of valid rule numbers)
    for field in range(len(valid_tickets[0])):
        valid_rule_numbers = set(range(len(rules)))

        for ticket_number in range(len(valid_tickets)):
            value = valid_tickets[ticket_number][field]
            invalid_rule_numbers = set()

            for rule_number in valid_rule_numbers:
                valid = False

                for range_tuple in rules[rule_number].ranges:
                    if value >= range_tuple[0] and value <= range_tuple[1]:
                        valid = True
                        break

                if not valid:
                    invalid_rule_numbers.add(rule_number)

            for invalid_rule_number in invalid_rule_numbers:
                valid_rule_numbers.remove(invalid_rule_number)

        valid_rule_numbers_per_field[field] = valid_rule_numbers

    # Build the field-rule map by infering the corresponding rule for each field
    # (the input is guaranteed to produce a cascading number of valid rules):
    field_rule_map = {}  # (key: field, value: rule number - both are 0-based)

    while len(valid_rule_numbers_per_field) > 0:
        # Find the field with one 1 valid rule:
        for field, valid_rule_numbers in valid_rule_numbers_per_field.items():
            if len(valid_rule_numbers) == 1:
                break

        # Add this field-rule relation to the map:
        rule_number = next(iter(valid_rule_numbers))
        field_rule_map[field] = rule_number

        # Remove this rule from the rest of the valid rules:
        valid_rule_numbers_per_field.pop(field)
        for _, valid_rule_numbers in valid_rule_numbers_per_field.items():
            valid_rule_numbers.remove(rule_number)

    return field_rule_map

def get_values(file_name, field_contains):
    rules, personal_ticket, nearby_tickets = _read_rules_and_tickets(file_name)
    field_rule_map = _get_field_rule_map(rules, personal_ticket, nearby_tickets)

    relevant_rule_numbers = set()
    for rule_number, rule in enumerate(rules):
        if field_contains in rule.name:
            relevant_rule_numbers.add(rule_number)

    relevant_fields = []
    for field, rule_number in field_rule_map.items():
        if rule_number in relevant_rule_numbers:
            relevant_fields.append(field)

    return [personal_ticket[field] for field in relevant_fields]

print(reduce(lambda x, y: x * y, get_values("input.txt", "departure")))
