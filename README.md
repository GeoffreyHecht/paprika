# paprika

Paprika is a powerful toolkit to detect some code smells in analysed Android applications.

# Table of contents
*   [Code smells detection](#code_smells_detection)
*   [How to use it?](#how_to_use_it)
*   [Troubleshooting](#troubleshootings)
*   [Credits](#credits)
*   [Publications](#publications)

### <a name="code_smells_detection"></a>Code smells detection

Paprika currently supports the following Object-Oriented (OO) and Android code smells.

**Object-Oriented** code smells:
* Blob Class (BLOB),
* Swiss Army Knife (SAK),
* Long Method (LM),
* Complex Class (CC),
* Long Parameter List (LPL).

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
* Debuggable Release (DR),
* Durable Wakelock (DW),
* Public Data (PD),
* Rigid AlarmManager (RAM).

A more detailed description of most of the antipatterns is available [here](Antipatterns.md).

### <a name="hoz_to_use_it"></a>How to use it ?

Paprika needs an Android platform to works. It also requires a 64 bits version of Java.
You can find the correct folder structure for Android platforms in [this Github repository](https://github.com/Sable/android-platforms).
To compile Paprika into a jar with its dependencies, run `gradle shadowjar`.
You can find the built java application in ```build/libs/Paprika.jar```.

Note that Paprika might not work properly on apk files using a minimum sdk version superior or equal to 26.

Paprika has multiple execution modes depending on the first argument.

The **analyse** mode will allows you to scan with [Soot](https://sable.github.io/soot/) your Application application,
to detect contained code smells.
If analyzing multiple apks, it is recommended to put them in a folder and pass the folder path to Paprika rather 
than executing it multiple times, for better performance.
While in "folder mode", the arguments used to set a specific name (-n), key (-k) or package (-p) will be ignored.

After analyzing, you can use the **query** mode on your Neo4J graph to request how much code smells your application contains.

You may also use the **delete** mode to remove an application from the database.

#### Analyzing a single app

The `-omp` optional argument is recommended to avoid analyzing the libraries used by an application.

```
usage: paprika analyse [-h] -a ANDROIDJARS -db DATABASE [-n NAME] [-p PACKAGE] [-k KEY] [-dev DEVELOPER]
               [-cat CATEGORY] [-nd NBDOWNLOAD] [-d DATE] [-r RATING] [-pr PRICE] [-s SIZE] [-u]
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
  -h, --help             Show help message and exit
  -n NAME, --name NAME   Name of the application, defaults to apk filename
  -p PACKAGE, --package PACKAGE
                           Application main package (extracted from manifest)
  -k KEY, --key KEY      Key of the application
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

If you want to edit the properties of the applications individually, you'll have to include a JSON file in the folder, 
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
	
	// Alternative syntax to avoid repetition
	category: [
		{ value:"myCustomCategory",  apps:[ "anApk", "anotherApk" ] },
		{ value:"myOtherCategory", apps:[ "anApk" ] },
	]
	rating: [
		{ value:"3.5", apps:[ "anApk", "anotherApk"] }
	]
	
	// If you're using both syntaxes, the standard one has priority over the alternative.
}
```


#### Querying the database

```
usage: paprika query [-h] -db DATABASE -r REQUEST [-c CSV] [-k KEY] [-p PACKAGE] [-d] [-thr PATH]

required arguments:
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder
  -r REQUEST, --request REQUEST
                         Request to execute

optional arguments:
  -h, --help             Show help message and exit
  -c CSV, --csv CSV      Path to register csv files, defaults to working directory
  -p PACKAGE, --package PACKAGE
                         Package of the applications to delete
  -d, --details
                         Show the concerned entity in the results
  -thr PATH, --thresholds PATH
                         Read fuzzy patterns thresholds from properties file
```

There are several requests available:
* One for each of the antipatterns codes (NLMR, MIM...)
* All antipatterns at once (ALLAP)
* All heavy antipatterns at once: HAS, HSS, HBR (ALLHEAVY)
* Basic information about all apps analyzed (ANALYZED)

* Various statistics about applications (STATS)
* Lack of cohesion in methods of all classes (ALLLCOM)
* Class complexity of all classes (ALLCC)
* Cyclomatic complexity of all methods (ALLCYCLO)
* Number of methods of all classes (ALLNUMMETHODS)

* Count variables (COUNTVAR)
* Count inner classes (COUNTINNER)
* Count AsyncTasks (COUNTASYNC)
* Count Views (COUNTVIEWS)

* Only non fuzzy antipatterns: excludes OO code smells and HAS, HSS, HBR (NONFUZZY)
* Only fuzzy antipatterns (FUZZY)
* Only fuzzy antipatterns without fuzzing (FORCENOFUZZY)

#### Deleting an application from the database

```
usage: paprika delete [-h] -db DATABASE -dk KEY

required arguments:
  -db DATABASE, --database DATABASE
                         Path to neo4J Database folder
  -dk KEY, --deleteKey KEY
                         Key of the app to delete
                         
optional arguments:
  -h, --help             Show help message and exit
```


#### Example of usage
First, we analyze an app:

```
java -Xmx2G -XX:+UseConcMarkSweepGC -jar  Paprika.jar analyse -a "/path/to/android-platforms" -db "/path/to/database"
 -omp /path/to/apk.apk
```

Then we can launch queries on the database using query mode. For example:
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

Developed by [Geoffrey Hecht](http://geoffreyhecht.github.io/), 
[SOMCA](http://sofa.uqam.ca/somca.php),
[SPIRALS](https://team.inria.fr/spirals/) - [LATECE](http://www.latece.uqam.ca/)

### <a name="publications"></a>Publications

If you want to read more about how Paprika works, how you can use it or to cite us, we recommend you to read :

[__Tracking the Software Quality of Android Applications along their Evolution__](https://hal.inria.fr/hal-01178734)

[Detecting Antipatterns in Android Apps](https://hal.inria.fr/hal-01122754)

[An Empirical Study of the Performance Impacts of Android Code Smells](https://hal.inria.fr/hal-01276904)

[Investigating the Energy Impact of Android Smells](https://hal.inria.fr/hal-01403485)

[Détection et analyse de l’impact des défauts de code dans les applications mobiles (in French)](https://hal.inria.fr/tel-01418158)
