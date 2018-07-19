import csv
import os
import re
import shutil
import sys
import time
from subprocess import call

test_resources = "../resources"
apk_folder = test_resources + "/apk"
platform_folder = test_resources + "/android-platforms"
db_folder = "db"
csv_test_folder = "got"
expected_folder = "expected"
jar_location = "../../../build/libs/Paprika.jar"


test_data = [
    "dosbox", "opengpx", "openmanager",
    "passandroid", "tint", "tof", "wikipedia",
    "witness", "wordpress"
]

java_cmd = ["java", "-Xmx2G", "-XX:+UseConcMarkSweepGC", "-jar", jar_location]
analyse_args = ["analyse", "-a", platform_folder, "-db", db_folder, "-omp"]
query_args = ["query", "-db", db_folder, "-d", "-r", "ALLAP"]


def format_apk(app):
    return apk_folder + "/" + app + ".apk"


def analyse_paprika(apk):
    call(java_cmd + analyse_args + [format_apk(apk)])


def remove_csv_prefix(folder_name):
    regex = re.compile("[A-Z0-9]+\.csv$")
    for filename in os.listdir(folder_name):
        if filename.endswith(".csv"):
            os.rename(folder_name + "/" + filename,
                      folder_name + "/" + regex.search(filename).group())


def clean_folder(folder):
    print("Cleaning " + folder + " folder...")
    for filename in os.listdir(folder):
        path = folder + "/" + filename
        if not filename == "gitkeep":
            if os.path.isfile(path):
                os.remove(path)
            else:
                shutil.rmtree(path)


def get_columns_position(label_row):
    result = []
    for i in range(0, len(label_row)):
        if label_row[i] in ['app_key', 'n.app_key', 'm.app_key', 'full_name']:
            result.append(i)
    return result


def get_file_map(columns_to_check, reader):
    result = {}
    for line in reader:
        key = ""
        for index in columns_to_check:
            key += line[index] + ","
        result[key] = 0
    return result


def compare_csv(reference, test, filename):
    ref_reader = csv.reader(open(reference))
    test_reader = csv.reader(open(test))
    columns_to_check = get_columns_position(next(ref_reader))
    next(test_reader)
    ref_map = get_file_map(columns_to_check, ref_reader)
    test_map = get_file_map(columns_to_check, test_reader)
    not_found = []
    for key in ref_map:
        if key not in test_map:
            not_found.append(key)

    if len(ref_map) < len(test_map):
        offset = len(test_map) - len(ref_map)
        print("--------- " + filename + ": FOUND " + str(offset) + " MORE " + get_entry_label(offset)
              + " THAN EXPECTED -----------")
        extras = []
        for key in test_map:
            if key not in ref_map:
                extras.append(key)
        for item in extras:
            print(item)

    if len(not_found) != 0:
        print("--------- FAILURE: " + filename + " - " + str(len(not_found)) + " MISSING " +
              get_entry_label(len(not_found)) + " -----------")
        for item in not_found:
            print(item)
        print('\n')
        return False
    return True


def get_entry_label(count):
    if count == 1:
        return "ENTRY"
    else:
        return "ENTRIES"


def compare_files():
    success = True
    print("Comparing files...")
    for filename in os.listdir(expected_folder):
        path = expected_folder + "/" + filename
        if not filename.endswith(".csv"):
            continue
        if not compare_csv(path, csv_test_folder + "/" + filename, filename):
            success = False
        else:
            print("--------- SUCCESS: " + filename + " -----------\n")

    if success:
        print('--------- TEST SUCCESS -----------')
    else:
        print('--------- TEST FAILURE -----------')
        sys.exit(1)


def run_test():
    total_time = 0
    override = False
    if len(sys.argv) > 1 and sys.argv[1] == "--replace":
        override = True

    clean_folder(db_folder)
    if override:
        print("Overriding expected test data...")
    else:
        print("Starting test...")

    for data in test_data:
        start = time.time()
        print("Analyzing " + data)
        analyse_paprika(data)
        exec_time = time.time() - start
        total_time += exec_time
        print("Completed in " + "{0:.2f}".format(exec_time) + " seconds.")

    print("Total analysis time: " + "{0:.2f}".format(total_time) + " seconds")
    print("Querying results...")
    if override:
        clean_folder(expected_folder)
        call(java_cmd + query_args + ["-c", expected_folder + "/"])
        remove_csv_prefix(expected_folder)
        print("Done overriding.")
    else:
        clean_folder(csv_test_folder)
        call(java_cmd + query_args + ["-c", csv_test_folder + "/"])
        remove_csv_prefix(csv_test_folder)
        compare_files()


if __name__ == '__main__':
    run_test()
