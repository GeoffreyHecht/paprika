import os
import shutil
from subprocess import call

db_folder = "db"
db_out = "../../../../out/test/resources/db"
apk_path = "paprika-witness.apk"
platform_folder = "../android-platforms"
jar_location = "../../../../build/libs/Paprika.jar"
java_cmd = ["java", "-Xmx2G", "-XX:+UseConcMarkSweepGC", "-jar", jar_location]
analyse_args = ["analyse", "-a", platform_folder, "-db", db_folder, "-omp", "-u", "-p", "com.antipatterns.app"]


def clean_folder(folder):
    print("Cleaning " + folder + " folder...")
    for filename in os.listdir(folder):
        path = folder + "/" + filename
        if not filename == "gitkeep":
            if os.path.isfile(path):
                os.remove(path)
            else:
                shutil.rmtree(path)


def call_paprika(version):
    call(java_cmd + analyse_args + ["-tsdk", version, "-k", version, "-n", "witness-" + version, apk_path])


versions = [10, 13, 15, 16, 17, 22, 27]


def register_apk():
    try:
        clean_folder(db_out)
        shutil.rmtree(db_out)
    except FileNotFoundError:
        print("Folder " + db_out + " not found, unable to delete.")
    clean_folder(db_folder)
    for version in versions:
        print("Analyzing witness with android api " + str(version))
        call_paprika(str(version))
    shutil.copytree(db_folder, db_out)
    print("Test database successfully refreshed.")


if __name__ == '__main__':
    register_apk()
