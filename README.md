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
* Blob Class (BLOB),
* Swiss Army Knife (SAK),
* Long Method (LM),
* Complex Class (CC).

**Android** code smells:
* Internal Getter/Setter (IGS),
* Member Ignoring Method (MIM),
* No Low Memory Resolver (NLMR),
* Leaking Inner Class (LIC),
* UI Overdraw (UIO),
* Invalidate Without Rect (IWR),
* Heavy AsyncTask (HAS),
* Heavy Service Start (HSS),
* Heavy Broadcast Receiver (HBR),
* Init OnDraw (IOD),
* Hashmap Usage (HMU),
* Unsupported Hardware Acceleration (UHA),
* Bitmap Format Usage (BFU).

### <a name="hoz_to_use_it"></a>How to use it ?

Paprika needs an Android plaftorm to works.  
You can find many Android platforms in [this Github repository](https://github.com/Sable/android-platforms).  
You can find the java application in ```paprika/out/artifacts/Paprika_jar```.

You can choose between two modes: **analyse** and **query**.
The **analyse** mode will allows you to scan with [Soot](https://sable.github.io/soot/) your Application application, to detect contained code smells.
You can use after the **query** mode on your Neo4J graph to request how much code smells your application contains.
Note that you don't need to install Neo4J on your side since it's embedded into Paprika (however it can be useful if you want to visualize the database).

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
#### Example of usage
First we launch the analysis of an app (it can be done multiple times with differents apps into the same database) :

```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar analyse -a "/path/to/androidjars" -db "/path/to/database" -n "myapp" -p "mypackage.app" -k sha256oftheAPK -dev mydev -cat mycat -nd 100 -d "2017-01-001 10:23:39.050315" -r 1.0 -s 1024 -u "unsafe mode" /path/to/apk.apk
```

Then you can launch queries on this database using query mode, for example :
```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar query -db "/path/to/database" -d TRUE -r ALLAP
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

