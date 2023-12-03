# Part 1
def get_num_valid_passports(file_name):
    FIELDS = ["byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid"]
    remaining_fields = set(FIELDS)

    in_passport = (
        False  # (just here to catch the very last passport, in case it's valid)
    )

    num_valid_passports = 0
    with open(file_name) as input:
        for line in input:
            if line == "\n":
                in_passport = False
                if not remaining_fields:
                    num_valid_passports += 1
                remaining_fields = set(FIELDS)
            else:
                in_passport = True
                for field in line.split():
                    field_name = field[:3]
                    if field_name in remaining_fields:
                        remaining_fields.remove(field_name)

    if in_passport and not remaining_fields:
        num_valid_passports += 1

    return num_valid_passports

print(get_num_valid_passports("input.txt"))

# Part 2
def get_num_valid_passports_strict(file_name):
    FIELDS = ["byr", "iyr", "eyr", "hgt", "hcl", "ecl", "pid"]
    remaining_fields = set(FIELDS)

    in_passport = (
        False  # (just here to catch the very last passport, in case it's valid)
    )

    num_valid_passports = 0
    with open(file_name) as input:
        for line in input:
            if line == "\n":
                in_passport = False
                if not remaining_fields:
                    num_valid_passports += 1
                remaining_fields = set(FIELDS)
            else:
                in_passport = True
                for field in line.split():
                    valid = False
                    field_name = field[:3]
                    if field_name in remaining_fields:
                        data = field[4:]

                        if (
                            field_name == "byr"
                            and int(data) >= 1920
                            and int(data) <= 2002
                        ):
                            valid = True
                        elif (
                            field_name == "iyr"
                            and int(data) >= 2010
                            and int(data) <= 2020
                        ):
                            valid = True
                        elif (
                            field_name == "eyr"
                            and int(data) >= 2020
                            and int(data) <= 2030
                        ):
                            valid = True
                        elif field_name == "hgt":
                            units = data[-2:]
                            if units == "cm":
                                if int(data[:-2]) >= 150 and int(data[:-2]) <= 193:
                                    valid = True
                            elif units == "in":
                                if int(data[:-2]) >= 59 and int(data[:-2]) <= 76:
                                    valid = True
                        elif field_name == "hcl" and len(data) == 7 and data[0] == "#":
                            valid = True
                            for letter in data[1:]:
                                if letter not in "0123456789abcdef":
                                    valid = False
                                    break
                        elif field_name == "ecl" and data in set(
                            ["amb", "blu", "brn", "gry", "grn", "hzl", "oth"]
                        ):
                            valid = True
                        elif field_name == "pid" and len(data) == 9:
                            try:
                                valid = True
                            except:
                                pass

                        if valid:
                            remaining_fields.remove(field_name)

    if in_passport and not remaining_fields:
        num_valid_passports += 1

    return num_valid_passports

print(get_num_valid_passports_strict("input.txt"))
