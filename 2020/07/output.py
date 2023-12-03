class BagDetails:
    def __init__(self, quantity, type):
        self.quantity = quantity
        self.type = type

def extract_rules(file_name):
    rules = {}

    with open(file_name) as input:
        for line in input:
            split = line.strip().split(" bags contain ")
            container = split[0]
            contents = split[1][:-1]

            rules[container] = []

            if contents != "no other bags":
                for details in contents.split(", "):
                    fragments = details.split()
                    rules[container].append(
                        BagDetails(int(fragments[0]), fragments[1] + " " + fragments[2])
                    )

    return rules

def contains_bag(rules, container, target_bag, valid_ancestors):
    for bag_details in rules[container]:
        if bag_details.type == target_bag or bag_details.type in valid_ancestors:
            return {container}

        else:
            ancestors = contains_bag(
                rules, bag_details.type, target_bag, valid_ancestors
            )
            if ancestors:
                return {container} | ancestors

    return set()

# Part 1
def get_number_possible_bags(rules, target_bag):
    valid_ancestors = set()

    for container, _ in rules.items():
        if container == target_bag:
            continue

        ancestors = contains_bag(rules, container, target_bag, valid_ancestors)
        if ancestors:
            valid_ancestors = valid_ancestors | ancestors

    return len(valid_ancestors)

print(get_number_possible_bags(extract_rules("input.txt"), "shiny gold"))

# Part 2
def get_number_contained_bags(rules, target_bag):
    number_contained_bags = 0

    for bag_details in rules[target_bag]:
        number_contained_bags += bag_details.quantity * (
            1 + get_number_contained_bags(rules, bag_details.type)
        )

    return number_contained_bags


print(get_number_contained_bags(extract_rules("input.txt"), "shiny gold"))
