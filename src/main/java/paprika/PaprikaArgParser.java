/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprika;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static paprika.entities.PaprikaApp.NO_SDK;

public class PaprikaArgParser {

    public static final String ANALYSE_MODE_ARG = "analyse";
    public static final String APK_ARG = "apk";
    public static final String ANDROID_JARS_ARG = "androidJars";
    public static final String DATABASE_ARG = "database";
    public static final String NAME_ARG = "name";
    public static final String PACKAGE_ARG = "package";
    public static final String KEY_ARG = "key";
    public static final String DEVELOPER_ARG = "developer";
    public static final String CATEGORY_ARG = "category";
    public static final String NB_DOWNLOAD_ARG = "nbDownload";
    public static final String DATE_ARG = "date";
    public static final String RATING_ARG = "rating";
    public static final String SIZE_ARG = "size";
    public static final String UNSAFE_ARG = "unsafe";
    public static final String VERSION_CODE_ARG = "versionCode";
    public static final String VERSION_NAME_ARG = "versionName";
    public static final String TARGET_SDK_VERSION_ARG = "targetSdkVersion";
    public static final String SDK_VERSION_ARG = "sdkVersion";
    public static final String ONLY_MAIN_PACKAGE_ARG = "onlyMainPackage";
    public static final String PRICE_ARG = "price";
    public static final String QUERY_MODE_ARG = "query";
    public static final String REQUEST_ARG = "request";
    public static final String CSV_ARG = "csv";
    public static final String DEL_KEY_ARG = "delKey";
    public static final String DEL_PACKAGE_ARG = "delPackage";
    public static final String DETAILS_ARG = "details";
    public static final String THRESHOLDS_ARG = "thresholds";

    private static final String SUB_PARSER = "sub_command";
    private static final String DATE_REGEX =
            "^([0-9]{4})-([0-1][0-9])-([0-3][0-9])\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]*)$";

    private ArgumentParser parser;
    private Subparsers subparsers;
    private Subparser analyseParser;
    private Namespace res;

    public PaprikaArgParser() {
        parser = ArgumentParsers.newFor("paprika").build();
        subparsers = parser.addSubparsers().dest(SUB_PARSER);
        setupAnalyse();
        setupQuery();
    }

    public String computeSha256(String path) throws IOException, NoSuchAlgorithmException {
        byte[] buffer = new byte[2048];
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = new FileInputStream(path)) {
            while (true) {
                int readBytes = is.read(buffer);
                if (readBytes > 0)
                    digest.update(buffer, 0, readBytes);
                else
                    break;
            }
        }
        byte[] hashValue = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aHashValue : hashValue) {
            sb.append(Integer.toString((aHashValue & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public void handleError(ArgumentParserException e) {
        analyseParser.handleError(e);
    }

    private void setupAnalyse() {
        analyseParser = subparsers.addParser(ANALYSE_MODE_ARG).help("Analyse an app");
        analyseParser.addArgument(APK_ARG).help("Path of the APK to analyze");
        analyseParser.addArgument("-a", prefixArg(ANDROID_JARS_ARG)).required(true)
                .help("Path to android platforms jars");
        analyseParser.addArgument("-db", prefixArg(DATABASE_ARG)).required(true)
                .help("Path to neo4J Database folder (will be created if none is found on the given path)");
        analyseParser.addArgument("-n", prefixArg(NAME_ARG))
                .setDefault("")
                .help("Name of the application (or the apk filename will be used as name)");
        analyseParser.addArgument("-p", prefixArg(PACKAGE_ARG))
                .setDefault("")
                .help("Application main package");
        analyseParser.addArgument("-k", prefixArg(KEY_ARG))
                .help("sha256 of the apk used as identifier");
        analyseParser.addArgument("-dev", prefixArg(DEVELOPER_ARG)).setDefault("default-developer")
                .help("Application developer");
        analyseParser.addArgument("-cat", prefixArg(CATEGORY_ARG)).setDefault("default-category")
                .help("Application category");
        analyseParser.addArgument("-nd", prefixArg(NB_DOWNLOAD_ARG)).setDefault(0)
                .type(Integer.class)
                .help("Numbers of downloads for the app");
        analyseParser.addArgument("-d", prefixArg(DATE_ARG)).setDefault("2017-01-01 10:23:39.050315")
                .help("Date of download");
        analyseParser.addArgument("-r", prefixArg(RATING_ARG)).type(Double.class)
                .setDefault(1.0)
                .help("application rating");
        analyseParser.addArgument("-pr", prefixArg(PRICE_ARG)).setDefault("Free")
                .help("Price of the application");
        analyseParser.addArgument("-s", prefixArg(SIZE_ARG)).type(Integer.class)
                .setDefault(1)
                .help("Size of the application");
        analyseParser.addArgument("-u", prefixArg(UNSAFE_ARG))
                .action(Arguments.storeTrue())
                .help("Unsafe mode (no args checking)");
        analyseParser.addArgument("-vc", prefixArg(VERSION_CODE_ARG))
                .setDefault("")
                .help("Version Code of the application (extract from manifest)");
        analyseParser.addArgument("-vn", prefixArg(VERSION_NAME_ARG)).setDefault("")
                .help("Version Name of the application (extract from manifest)");
        analyseParser.addArgument("-tsdk", prefixArg(TARGET_SDK_VERSION_ARG))
                .setDefault(NO_SDK)
                .type(Integer.class)
                .help("Target SDK Version (extract from manifest)");
        analyseParser.addArgument("-sdk", prefixArg(SDK_VERSION_ARG))
                .setDefault(NO_SDK)
                .type(Integer.class)
                .help("sdk version (extract from manifest)");
        analyseParser.addArgument("-omp", prefixArg(ONLY_MAIN_PACKAGE_ARG))
                .action(Arguments.storeTrue())
                .help("Analyze only the main package of the application");
    }

    private void setupQuery() {
        Subparser queryParser = subparsers.addParser(QUERY_MODE_ARG).help("Query the database");
        queryParser.addArgument("-db", prefixArg(DATABASE_ARG)).required(true)
                .help("Path to neo4J Database folder");
        queryParser.addArgument("-r", prefixArg(REQUEST_ARG)).help("Request to execute");
        queryParser.addArgument("-c", prefixArg(CSV_ARG)).help("path to register csv files").setDefault("");
        queryParser.addArgument("-dk", prefixArg(DEL_KEY_ARG)).help("key to delete");
        queryParser.addArgument("-dp", prefixArg(DEL_PACKAGE_ARG)).help("Package of the applications to delete");
        queryParser.addArgument("-d", prefixArg(DETAILS_ARG))
                .action(Arguments.storeTrue())
                .help("Show the concerned entity in the results");
        queryParser.addArgument("-thr", prefixArg(THRESHOLDS_ARG))
                .setDefault((String) null)
                .help("Path to .properties file containing android thresholds");
    }
    
    private String prefixArg(String arg) {
        return "--" + arg;
    }

    public void parseArgs(String[] args) throws ArgumentParserException,
            IOException, NoSuchAlgorithmException, PaprikaArgException {
        res = parser.parseArgs(args);
        if (isAnalyseMode() && res.get(UNSAFE_ARG) == null) {
            checkArgs();
        }
    }

    public Namespace getArguments() {
        return res;
    }

    public boolean isAnalyseMode() {
        return res.getString(SUB_PARSER).equals(ANALYSE_MODE_ARG);
    }

    public boolean isQueryMode() {
        return res.getString(SUB_PARSER).equals(QUERY_MODE_ARG);
    }

    public String getSha() throws IOException, NoSuchAlgorithmException {
        if (res.getString(KEY_ARG) == null) {
            return computeSha256(res.getString(APK_ARG));
        } else {
            return res.getString(KEY_ARG).toLowerCase();
        }
    }

    public void checkArgs() throws PaprikaArgException, IOException, NoSuchAlgorithmException {
        if (res.getString(KEY_ARG) != null) {
            String sha256 = computeSha256(res.getString(APK_ARG));
            if (!sha256.equals(res.getString(KEY_ARG).toLowerCase())) {
                throw new PaprikaArgException("The given key is different from sha256 of the apk");
            }
        }
        if (!res.getString(DATE_ARG).matches(DATE_REGEX)) {
            throw new PaprikaArgException("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
        }
    }

    public List<String> getAppsPaths() {
        List<String> apps = new ArrayList<>();
        File apkFolder = new File(res.getString(APK_ARG));
        if (!apkFolder.isDirectory()) {
            return Collections.singletonList("");
        } else {
            File[] files = apkFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".apk")) {
                        apps.add(res.getString(APK_ARG) + File.separator + file.getName());
                    }
                }
            }
        }
        return apps;
    }

    public boolean isFolderMode() {
        File apkFolder = new File(res.getString(APK_ARG));
        return apkFolder.isDirectory();
    }


}
