# Design note: A naive approach might be to generate all possible
# strings that match the given ruleset, but that grows exponentially
# with the input - the input in this folder contains 118 '|' characters,
# so the worst case is that there are 2^118 different strings to check.

class ScanningMode:
    rules = 1
    messages = 2

def read_input(file_name):
    rules = {}
    messages = []

    scanning_mode = ScanningMode.rules
    with open(file_name) as file:
        for line in file:
            line = line.strip()

            if scanning_mode == ScanningMode.rules:
                if line:
                    split = line.split(": ")
                    rule_number = int(split[0])

                    rule = []
                    for subrules in split[1].split(" | "):
                        possible_subrule = []
                        for subrule in subrules.split(" "):
                            if subrule[0] == '"' and subrule[-1] == '"':  # (string)
                                possible_subrule.append(subrule[1:-1])
                            else:  # (another rule)
                                possible_subrule.append(int(subrule))
                        rule.append(possible_subrule)

                    rules[rule_number] = rule

                else:
                    scanning_mode = ScanningMode.messages

            elif scanning_mode == ScanningMode.messages:
                if line:
                    messages.append(line)
                else:
                    scanning_mode = None

            else:
                raise "Unknown scanning mode!"

    return (rules, messages)

def message_matches_subrule_fragment(rules, message, subrule_fragment):
    # If the fragment is another rule number, return the matched lengths for that rule:
    if isinstance(subrule_fragment, int):
        return message_matches_rule(rules, message, subrule_fragment)

    # Otherwise, if the fragment is an actual string to match...
    else:

        # ...if the fragment matches the start of the message, return the length of the match:
        if subrule_fragment == message[: len(subrule_fragment)]:
            return {len(subrule_fragment)}

        # ...otherwise, return an empty set:
        else:
            return set()

def message_matches_subrule(rules, message, subrule):
    matched_lengths = set()

    for subrule_fragment in subrule:
        if not matched_lengths:
            new_matched_lengths = message_matches_subrule_fragment(
                rules, message, subrule_fragment
            )

            if not new_matched_lengths:
                break

            else:
                matched_lengths.update(new_matched_lengths)

        else:
            new_matched_lengths = set()

            for matched_length in matched_lengths:
                new_matched_lengths.update(
                    [
                        matched_length + new_matched_length
                        for new_matched_length in message_matches_subrule_fragment(
                            rules, message[matched_length:], subrule_fragment
                        )
                    ]
                )

            if not new_matched_lengths:
                matched_lengths.clear()
                break

            else:
                matched_lengths = new_matched_lengths

    return matched_lengths

def message_matches_rule(rules, message, rule):
    matched_lengths = set()

    for subrule in rules[rule]:
        matched_lengths.update(message_matches_subrule(rules, message, subrule))

    return matched_lengths

def get_number_of_valid_messages(rules, messages, rule_to_match):
    number_of_valid_messages = 0

    for message in messages:
        if len(message) in message_matches_rule(rules, message, rule_to_match):
            number_of_valid_messages += 1

    return number_of_valid_messages

# Part 1
rules, messages = read_input("input_part_1.txt")
print(get_number_of_valid_messages(rules, messages, 0))

# Part 2
rules, messages = read_input("input_part_2.txt")
print(get_number_of_valid_messages(rules, messages, 0))
