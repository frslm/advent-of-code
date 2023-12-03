class Food:
    def __init__(self, ingredients=None, allergens=None):
        self.ingredients = set(ingredients) if ingredients is not None else set()
        self.allergens = set(allergens) if allergens is not None else set()

    @classmethod
    def intersect(cls, lhs, rhs):
        return cls(
            [i for i in lhs.ingredients if i in rhs.ingredients],
            [a for a in lhs.allergens if a in rhs.allergens],
        )

    def __str__(self):
        return (
            "Ingredients: "
            + str(self.ingredients)
            + "\nAllergens: "
            + str(self.allergens)
            + "\n"
        )

def read_input(file_name):
    foods = []

    with open(file_name) as file:
        for line in file:
            line = line.strip()

            split = line.split(" (contains ")
            ingredients = set(split[0].split(" "))
            allergens = set(split[1][:-1].split(", "))
            # Assumption: No ingredient or allergen duplicates in a given food.

            foods.append(Food(ingredients, allergens))

    return foods

def intersect_foods_with_common_allergen(foods, allergen):
    intersected_food = None

    for food in foods:
        if allergen in food.allergens:
            intersected_food = (
                food
                if intersected_food is None
                else Food.intersect(intersected_food, food)
            )

    return intersected_food

def get_ingredient_allergen_map(foods):
    # For each food, add its list of allergens to the global list:
    allergens = set()
    for food in foods:
        allergens.update(food.allergens)

    # For each possible allergen, get the intersection of all foods that contain this allergen:
    intersected_foods = [
        intersect_foods_with_common_allergen(foods, allergen) for allergen in allergens
    ]

    # While there are some intersected foods...
    ingredient_allergen_map = {}
    while intersected_foods:

        # ...find an intersected food with only a single ingredient (which is guaranteed to contain only a single allergen):
        found = False
        for index, intersected_food in enumerate(intersected_foods):
            if len(intersected_food.ingredients) == 1:
                found = True
                break
        assert found  # (if hit, then the list of intersected foods alone doesn't have enough information; need to use the original set of foods too)

        # ...add it to the map of known ingredient-allergen associations:
        ingredient = next(iter(intersected_food.ingredients))
        allergen = next(iter(intersected_food.allergens))
        ingredient_allergen_map[ingredient] = allergen

        # ...remove this intersected food from the list:
        intersected_foods.pop(index)

        # ...remove all mentions of this ingredient-allergen pair from the other intersected foods:
        for intersected_food in intersected_foods:
            if ingredient in intersected_food.ingredients:
                intersected_food.ingredients.remove(ingredient)

            if allergen in intersected_food.allergens:
                intersected_food.allergens.remove(allergen)

    return ingredient_allergen_map

def get_unknown_ingredient_occurences(foods, ingredient_allergen_map):
    unknown_ingredient_occurences = {}

    # For each ingredient of each food...
    for food in foods:
        for ingredient in food.ingredients:

            # ...if it's not a known ingredient (part of the ingredient-allergen map)...
            if ingredient not in ingredient_allergen_map:

                # ...update the number of this ingredient's occurences:
                if ingredient not in unknown_ingredient_occurences:
                    unknown_ingredient_occurences[ingredient] = 0
                unknown_ingredient_occurences[ingredient] += 1

    return unknown_ingredient_occurences

def canonical_dangerous_ingredient_list(ingredient_allergen_map):
    return ",".join(
        [
            item[0]
            for item in sorted(
                ingredient_allergen_map.items(), key=lambda item: item[1]
            )
        ]
    )

# Part 1
foods = read_input("input.txt")
ingredient_allergen_map = get_ingredient_allergen_map(foods)
unknown_ingredient_occurences = get_unknown_ingredient_occurences(
    foods, ingredient_allergen_map
)
print(sum([num for num in unknown_ingredient_occurences.values()]))

# Part 2
print(canonical_dangerous_ingredient_list(ingredient_allergen_map))
