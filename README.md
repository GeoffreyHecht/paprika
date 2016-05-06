# paprika

Paprika is a powerfull toolkit to detect some code smells in analysed Android applications.

# Table of contents
*   [Code smells detection](#code_smells_detection)
*   [How to use it?](#how_to_use_it)
*   [Troubleshootings](#troubleshootings)
*   [Credits](#credits)

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
* Bitmap Format Usage.

### <a name="hoz_to_use_it"></a>How to use it ?

Paprika needs an Android plaftorm to works.  
You can find many Android platforms in [this Github repository](https://github.com/Sable/android-platforms).  
You can find the java application in ```paprika/out/artifacts/Paprika_jar```.

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

optional arguments:
  -h, --help             show this help message and exit
  -a ANDROIDJARS, --androidJars ANDROIDJARS
                         Path to android platforms jars
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder
  -n NAME, --name NAME   Name of the application
  -p PACKAGE, --package PACKAGE
                         Application main package
  -k KEY, --key KEY      sha256 of the apk used as identifier
  -dev DEVELOPER, --developer DEVELOPER
                         Application developer
  -cat CATEGORY, --category CATEGORY
                         Application category
  -nd NBDOWNLOAD, --nbDownload NBDOWNLOAD
                         Numbers of downloads for the app
  -d DATE, --date DATE   Date of download
  -r RATING, --rating RATING
                         application rating
  -pr PRICE, --price PRICE
                         Price of the application
  -s SIZE, --size SIZE   Size of the application
  -u UNSAFE, --unsafe UNSAFE
                         Unsafe mode (no args checking)
  -vc VERSIONCODE, --versionCode VERSIONCODE
                         Version Code of the application (extract from manifest)
  -vn VERSIONNAME, --versionName VERSIONNAME
                         Version Name of the application (extract from manifest)
  -tsdk TARGETSDKVERSION, --targetSdkVersion TARGETSDKVERSION
                         Target SDK Version (extract from manifest)
  -sdk SDKVERSION, --sdkVersion SDKVERSION
                         sdk version (extract from manifest)
  -omp ONLYMAINPACKAGE, --onlyMainPackage ONLYMAINPACKAGE
                         Analyze only the main package of the application
```

#### Query mode usage

```
usage: paprika query [-h] -db DATABASE [-r REQUEST] [-c CSV] [-k KEY] [-p PACKAGE] [-d DETAILS]

optional arguments:
  -h, --help             show this help message and exit
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder
  -r REQUEST, --request REQUEST
                         Request to execute
  -c CSV, --csv CSV      path to register csv files
  -k KEY, --key KEY      key to delete
  -p PACKAGE, --package PACKAGE
                         Package of the applications to delete
  -d DETAILS, --details DETAILS
                         Show the concerned entity in the results
```

### <a name="troubleshootings"></a>Troubleshootings

**paprika** is still in development.  
Found a bug? We'd love to know about it!  
Please report all issues on the github issue tracker.

### License

GNU AFFERO GENERAL PUBLIC LICENSE (version 3)

### <a name="credits"></a>Credits

Developed by [Geoffrey Hecht](http://geoffreyhecht.github.io/), [SOMCA](http://sofa.uqam.ca/somca.php), [SPIRALS](https://team.inria.fr/spirals/) - [LATECE](http://www.latece.uqam.ca/)
