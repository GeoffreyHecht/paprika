# paprika

Paprika is a powerfull toolkit to detect some code smells in analysed Android applications.

# Table of contents
*   [Code smells detection](#code_smells_detection)
*   [How to use it?](#how_to_use_it)
*   [Troubleshootings](#troubleshootings)
*   [Credits](#credits)
*   [Publications](#publications)

### <a name="code_smells_detection"></a>Code smells detection

Paprika supports currently 16 Object-Oriented (OO) and Android code smells.

**Object-Oriented** code smells:
* Blob Class,
* Swiss Army Knife,
* Long Method,
* Complex Class.

**Android** code smells:
* Internal Getter/Setter,
* Member Ignoring Method,
* No Low Memory Resolver,
* Leaking Inner Class,
* UI Overdraw,
* Invalidate Without Rect,
* Heavy AsyncTask,
* Heavy Service Start,
* Heavy Broadcast Receiver,
* Init OnDraw,
* Hashmap Usage,
* Unsupported Hardware Acceleration,

### <a name="hoz_to_use_it"></a>How to use it ?

Paprika needs an Android platform to works. It also requires a 64 bits version of Java.
You can find many Android platforms with the correct folder structure [this Github repository](https://github.com/Sable/android-platforms).
To compile Paprika into a jar with its dependencies, run `gradle shadowjar`.
You can find the java application in ```build/libs/Paprika.jar```.

Please note that Paprika is not working correctly with Java 9, we recommend to launch it with Java 7.
You can choose between two modes: **analyse** and **query**.
The **analyse** mode will allows you to scan with [Soot](https://sable.github.io/soot/) your Application application, to detect contained code smells.
You can use after the **query** mode on your Neo4J graph to request how much code smells your application contains.

#### Analyse mode usage

```
usage: paprika analyse [-h] -a ANDROIDJARS -db DATABASE -n NAME -p PACKAGE -k KEY -dev DEVELOPER
               -cat CATEGORY -nd NBDOWNLOAD -d DATE -r RATING [-pr PRICE] -s SIZE [-u UNSAFE]
               [-vc VERSIONCODE] [-vn VERSIONNAME] [-tsdk TARGETSDKVERSION] [-sdk SDKVERSION]
               [-omp ONLYMAINPACKAGE] apk

positional arguments:
  apk                    Path of the APK to analyze

required arguments:
  -a ANDROIDJARS, --androidJars ANDROIDJARS
                         Path to android platforms jars
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder
  -p PACKAGE, --package PACKAGE
                         Application main package
  -n NAME, --name NAME   Name of the application

optional arguments:
  -h, --help             show this help message and exit
  -k KEY, --key KEY      sha256 of the apk used as identifier
  -dev DEVELOPER, --developer DEVELOPER
                         Application developer, defaults to "default-dev"
  -cat CATEGORY, --category CATEGORY
                         Application category, defaults to "default-category"
  -nd NBDOWNLOAD, --nbDownload NBDOWNLOAD
                         Numbers of downloads for the app, defaults to 0
  -d DATE, --date DATE   Date of download, defaults to "2017-01-01 10:23:39.050315"
  -r RATING, --rating RATING
                         Application rating, defaults to 1.0
  -pr PRICE, --price PRICE
                         Price of the application, defaults to Free
  -s SIZE, --size SIZE   Size of the application, defaults to 1
  -u UNSAFE, --unsafe UNSAFE
                         Unsafe mode (no args checking)
  -vc VERSIONCODE, --versionCode VERSIONCODE
                         Version Code of the application (extract from manifest), empty by default
  -vn VERSIONNAME, --versionName VERSIONNAME
                         Version Name of the application (extract from manifest), empty by default
  -tsdk TARGETSDKVERSION, --targetSdkVersion TARGETSDKVERSION
                         Target SDK Version (extract from manifest), empty by default
  -sdk SDKVERSION, --sdkVersion SDKVERSION
                         SDK version (extract from manifest), empty by default
  -omp, --onlyMainPackage
                         Analyze only the main package of the application
```

#### Query mode usage

```
usage: paprika query [-h] -db DATABASE [-r REQUEST] [-c CSV] [-k KEY] [-p PACKAGE] [-d DETAILS]

required arguments:
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder
  -r REQUEST, --request REQUEST
                         Request to execute

optional arguments:
  -h, --help             Show this help message and exit
  -c CSV, --csv CSV      Path to register csv files defaults to working directory
  -k KEY, --key KEY      Key to delete
  -p PACKAGE, --package PACKAGE
                         Package of the applications to delete
  -d, --details
                         Show the concerned entity in the results
  -thr, --thresholds     Read fuzzy patterns thresholds from properties file
```

#### Example of usage
First we launch the analysis of an app (it can be done multiple times with different apps into the same database) :

```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar analyse -a "/path/to/android-platforms" -db "/path/to/database"
-n "myapp" -p "mypackage.app" -omp /path/to/apk.apk
```

Then you can launch queries on this database using query mode, for example :
```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar query -db "/path/to/database" -d -r ALLAP
```

### <a name="troubleshootings"></a>Troubleshootings

**paprika** is still in development.  
Found a bug? We'd love to know about it!  
Please report all issues on the github issue tracker.

### License

GNU AFFERO GENERAL PUBLIC LICENSE (version 3)

### <a name="credits"></a>Credits

Developed by [Geoffrey Hecht](http://geoffreyhecht.github.io/), [SOMCA](http://sofa.uqam.ca/somca.php), [SPIRALS](https://team.inria.fr/spirals/) - [LATECE](http://www.latece.uqam.ca/)

### <a name="publications"></a>Publications

If you want to read more about how Paprika work, how you can use it or to cite us, we recommend you to read :

[__Tracking the Software Quality of Android Applications along their Evolution__](https://hal.inria.fr/hal-01178734)

[Detecting Antipatterns in Android Apps](https://hal.inria.fr/hal-01122754)

[An Empirical Study of the Performance Impacts of Android Code Smells](https://hal.inria.fr/hal-01276904)

[Investigating the Energy Impact of Android Smells](https://hal.inria.fr/hal-01403485)

[Détection et analyse de l’impact des défauts de code dans les applications mobiles (in French)](https://hal.inria.fr/tel-01418158)
