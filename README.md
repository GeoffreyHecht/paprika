# paprika

Paprika is a powerfull toolkit to detect some code smells in analysed Android applications.

# Table of contents
*   [Code smells detection](#code_smells_detection)
*   [How to use it?](#how_to_use_it)
*   [Troubleshooting](#troubleshootings)
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

Note that Paprika might not work properly on apk files using a minimum sdk version superior or equal to 26.

You can choose between two modes: **analyse** and **query**.

The **analyse** mode will allows you to scan with [Soot](https://sable.github.io/soot/) your Application application,
to detect contained code smells.
If analyzing multiple apks, it is recommended to put them in a folder and pass the folder path to Paprika rather 
than executing it multiple times. This will be up to 2x times faster
While in "folder mode", the arguments used to set a specific name (-n), key (-k) or package (-p) will be ignored.

After analyzing, you can use the **query** mode on your Neo4J graph to request how much code smells your application contains.

#### Analyze a single app

```
usage: paprika analyse [-h] -a ANDROIDJARS -db DATABASE -n NAME -p PACKAGE -k KEY -dev DEVELOPER
               -cat CATEGORY -nd NBDOWNLOAD -d DATE -r RATING [-pr PRICE] -s SIZE [-u]
               [-vc VERSIONCODE] [-vn VERSIONNAME] [-tsdk TARGETSDKVERSION] [-sdk SDKVERSION]
               [-omp] apk

positional arguments:
  apk                    Path of the APK to analyze

required arguments:
  -a ANDROIDJARS, --androidJars ANDROIDJARS
                         Path to android platforms jars
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder

optional arguments:
  -h, --help             show this help message and exit
  -n NAME, --name NAME   Name of the application, defaults to apk filename
  -p PACKAGE, --package PACKAGE
                           Application main package (extracted from manifest)
  -k KEY, --key KEY      sha256 of the apk used as identifier
  -dev DEVELOPER, --developer DEVELOPER
                         Application developer, defaults to "default-dev"
  -cat CATEGORY, --category CATEGORY
                         Application category, defaults to "default-category"
  -nd NBDOWNLOAD, --nbDownload NBDOWNLOAD
                         Numbers of downloads for the app, defaults to 0
  -d DATE, --date DATE   Date of download, defaults to "2017-01-28 00:00:00.000000"
  -r RATING, --rating RATING
                         Application rating, defaults to 1.0
  -pr PRICE, --price PRICE
                         Price of the application, defaults to Free
  -s SIZE, --size SIZE   Size of the application, defaults to 1
  -u UNSAFE, --unsafe
                         Unsafe mode (no args checking)
  -vc VERSIONCODE, --versionCode VERSIONCODE
                         Version Code of the application, empty by default
  -vn VERSIONNAME, --versionName VERSIONNAME
                         Version Name of the application, empty by default
  -tsdk TARGETSDKVERSION, --targetSdkVersion TARGETSDKVERSION
                         Target SDK Version (extracted from manifest)
  -sdk SDKVERSION, --sdkVersion SDKVERSION
                         SDK version, empty by default
  -omp, --onlyMainPackage
                         Analyze only the main package of the application
```

#### Analyzing multiple applications at once

When using a folder in place of the **apk** parameter, Paprika will look into the contents of the folder and analyze every
.apk file it finds. The Paprika arguments used to set an application property (such as `-dev, --developer` or `-cat, --category`) will be
applied to every application analyzed (every app will have the same category). Note that the arguments used to set the name, key
or package of the application will not work (`-n, --name / -k, --key / -p, --package`).

If you want to edit properties to the applications individually, you'll have to include a JSON file in the folder, 
named `apk-properties.json`. The properties read from this file will override the values read from the standard
Paprika arguments.

Below is an example of the expected JSON format. The ids of the properties (name, category...) correspond to the Paprika argument names.
You do not have to include all the properties - if one is missing, the default value will be used.

```
{ 
	// Standard syntax
	apk_filename_without_extension: {
		name: "myCustomName"
		category: "myCustomCategory"
		nbDownload: "58"
		...
	}
	
	// Alternative syntax to avoid repeating yourself
	category: [
		{ value:"myCustomCategory",  apps:[ "anApk", "anotherApk" ] },
		{ value:"myOtherCategory", apps:[ "anApk" ] },
	]
	rating: [
		{ value:"3.5", apps:[ "anApk", "anotherApk"] }
	]
	
	// If you're using both syntax, the standard one has priority over the alternative.
}
```


#### Query mode usage

```
usage: paprika query [-h] -db DATABASE [-r REQUEST] [-c CSV] [-k KEY] [-p PACKAGE] [-d] [-thr PATH]

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
  -thr PATH, --thresholds PATH
                         Read fuzzy patterns thresholds from properties file
```

#### Example of usage
First we launch the analysis of an app (it can be done multiple times with different apps into the same database) :

```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar analyse -a "/path/to/android-platforms" -db "/path/to/database"
 -omp /path/to/apk.apk
```

Then you can launch queries on this database using query mode, for example :
```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar query -db "/path/to/database" -d -r ALLAP
```

### <a name="troubleshootings"></a>Troubleshooting

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
