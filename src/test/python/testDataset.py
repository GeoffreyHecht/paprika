import csv
import filecmp
import os
import re
import shutil
import sys
from subprocess import call


class TestApk:

    def __init__(self, name, package, sha, apk):
        self.name = name
        self.package = package
        self.sha = sha
        self.developer = "testDev"
        self.category = "testCat"
        self.downloads = "1"
        self.date = "2017-01-01 10:23:39.050315"
        self.rating = "1.0"
        self.size = "1024"
        self.apk = apk

    def get_paprika_params(self):
        return ["-n", self.name, "-p", self.package, "-k", self.sha, "-dev", self.developer,
                "-cat", self.category, "-nd", self.downloads, "-d", self.date, "-r", self.rating,
                "-s", self.size, self.apk]


test_resources = "../resources"
apk_folder = test_resources + "/apk"
platform_folder = test_resources + "/android-platforms"
db_folder = "db"
csv_test_folder = "got"
expected_folder = "expected"
jar_location = "../../../build/libs/Paprika.jar"


def format_apk(app):
    return apk_folder + "/" + app + ".apk"


test_data = [
    TestApk("witness", "com.antipatterns.app",
            "30ef6f312880442ea01d177d744e652cc2b25c1548ba16d33b7d1b2dcfa37c60",
            format_apk("com.antipatterns.app")),

    TestApk("openmanager", "org.braindroid.openmanager",
            "16d11c6c79d678c3a99d58cced02b565ccd9b1267adc1c1dfdf4ad1a29d0dd72",
            format_apk("org.brandroid.openmanager_212")),

    TestApk("dosbox", "org.hystudio.android.dosbox",
            "7edc9be5d5d97d612eaa7e8c07593e2de20d7239ebde0742d32c1bb1f9fc7018",
            format_apk("org.hystudio.android.dosbox_20500")),

    TestApk("passandroid", "org.ligi.passandroid",
            "a61e97c3ee8b9c0602327e9cc8274fa9e8e2ae7285db17e18bc59053fde527b4",
            format_apk("org.ligi.passandroid_255")),

    TestApk("opengpx", "org.opengpx",
            "6383ed45b83a20687e9c1d642ee3fd79451d270f066b20e234e0c756074928bf",
            format_apk("org.opengpx_192")),

    TestApk("tint", "org.tint",
            "077bee08f7f81593b8a4bd06dfe6e4b84d86aa7688e474effcdb63cdb0d3731b",
            format_apk("org.tint_10")),

    TestApk("tof", "org.tof",
            "66fd106572074e88e74abb74b4021629b0010c5ad42881692e3ddf8c0f5c5d4e",
            format_apk("org.tof_17")),

    TestApk("wikipedia", "org.wikipedia",
            "2c337bd33316b0894212dbca85436248c1c43019eb72cd3faa4241f7ae6d8e0e",
            format_apk("org.wikipedia_109")),

    TestApk("wordpress", "org.wordpress.android",
            "348aeef2fc408738d93bbcae294b068d5b161ddba01dc2edbd052d0fbf3a9303",
            format_apk("org.wordpress.android_103"))
]

java_cmd = ["java", "-Xmx2G", "-XX:+UseConcMarkSweepGC", "-jar", jar_location]
analyse_args = ["analyse", "-a", platform_folder, "-db", db_folder]
query_args = ["query", "-db", db_folder, "-d", "TRUE", "-r", "ALLAP"]


def analyse_paprika(apk):
    call(java_cmd + analyse_args + apk.get_paprika_params())


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


def compare_csv(reference, test):
    ref_reader = open(reference)
    test_reader = open(test)
    ref_map = {}
    test_map = {}
    for line in ref_reader.readlines():
        ref_map[line] = 0
    for line in test_reader.readlines():
        test_map[line] = 0

    for key, value in ref_map.items():
        if key not in test_map:
            print("EXPECTED BUT NOT FOUND: " + key)
            return False
    return True


def run_test():
    override = False
    if len(sys.argv) > 1 and sys.argv[1] == "--replace":
        override = True

    clean_folder(db_folder)
    if override:
        print("Overriding expected test data...")
    else:
        print("Starting test...")

    for data in test_data:
        print("Analyzing " + data.name)
        analyse_paprika(data)

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
        success = True

        print("Comparing files...")
        for filename in os.listdir(expected_folder):
            path = expected_folder + "/" + filename
            if not filename.endswith(".csv"):
                continue
            if not compare_csv(path, csv_test_folder + "/" + filename):
                success = False
                print("FAILURE: " + filename)

        if not success:
            sys.exit(1)

        print('SUCCESS')


if __name__ == '__main__':
    run_test()

