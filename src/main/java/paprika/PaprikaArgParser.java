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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PaprikaArgParser {

    private static final String DATE_REGEX =
            "^([0-9]{4})-([0-1][0-9])-([0-3][0-9])\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]*)$";
    private ArgumentParser parser;
    private Subparsers subparsers;
    private Subparser analyseParser;
    private Namespace res;

    public PaprikaArgParser() {
        parser = ArgumentParsers.newFor("paprika").build();
        subparsers = parser.addSubparsers().dest("sub_command");
        setupAnalyse();
        setupQuery();
    }

    private static String computeSha256(String path) throws IOException, NoSuchAlgorithmException {
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
        analyseParser = subparsers.addParser("analyse").help("Analyse an app");
        analyseParser.addArgument("apk").help("Path of the APK to analyze");
        analyseParser.addArgument("-a", "--androidJars").required(true)
                .help("Path to android platforms jars");
        analyseParser.addArgument("-db", "--database").required(true)
                .help("Path to neo4J Database folder (will be created if none is found on the given path)");
        analyseParser.addArgument("-n", "--name").required(true)
                .help("Name of the application (or the apk filename will be used as name)");
        analyseParser.addArgument("-p", "--package").required(true)
                .help("Application main package");
        analyseParser.addArgument("-k", "--key")
                .help("sha256 of the apk used as identifier");
        analyseParser.addArgument("-dev", "--developer").setDefault("default-developer")
                .help("Application developer");
        analyseParser.addArgument("-cat", "--category").setDefault("default-category")
                .help("Application category");
        analyseParser.addArgument("-nd", "--nbDownload").setDefault(0)
                .help("Numbers of downloads for the app");
        analyseParser.addArgument("-d", "--date").setDefault("2017-01-01 10:23:39.050315")
                .help("Date of download");
        analyseParser.addArgument("-r", "--rating").type(Double.class)
                .setDefault(1.0)
                .help("application rating");
        analyseParser.addArgument("-pr", "--price").setDefault("Free")
                .help("Price of the application");
        analyseParser.addArgument("-s", "--size").type(Integer.class)
                .setDefault(1)
                .help("Size of the application");
        analyseParser.addArgument("-u", "--unsafe").help("Unsafe mode (no args checking)");
        analyseParser.addArgument("-vc", "--versionCode").setDefault("")
                .help("Version Code of the application (extract from manifest)");
        analyseParser.addArgument("-vn", "--versionName").setDefault("")
                .help("Version Name of the application (extract from manifest)");
        analyseParser.addArgument("-tsdk", "--targetSdkVersion")
                .setDefault("")
                .help("Target SDK Version (extract from manifest)");
        analyseParser.addArgument("-sdk", "--sdkVersion")
                .setDefault("")
                .help("sdk version (extract from manifest)");
        analyseParser.addArgument("-omp", "--onlyMainPackage")
                .action(Arguments.storeTrue())
                .help("Analyze only the main package of the application");
    }

    private void setupQuery() {
        Subparser queryParser = subparsers.addParser("query").help("Query the database");
        queryParser.addArgument("-db", "--database").required(true)
                .help("Path to neo4J Database folder");
        queryParser.addArgument("-r", "--request").help("Request to execute");
        queryParser.addArgument("-c", "--csv").help("path to register csv files").setDefault("");
        queryParser.addArgument("-dk", "--delKey").help("key to delete");
        queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
        queryParser.addArgument("-d", "--details")
                .action(Arguments.storeTrue())
                .help("Show the concerned entity in the results");
    }

    public void parseArgs(String[] args) throws ArgumentParserException,
            IOException, NoSuchAlgorithmException, PaprikaArgException {
        res = parser.parseArgs(args);
        if (isAnalyseMode() && res.get("unsafe") == null) {
            checkArgs();
        }
    }

    public Namespace getArguments() {
        return res;
    }

    public boolean isAnalyseMode() {
        return res.getString("sub_command").equals("analyse");
    }

    public boolean isQueryMode() {
        return res.getString("sub_command").equals("query");
    }

    public String getSha() throws IOException, NoSuchAlgorithmException {
        if (res.getString("key") == null) {
            return computeSha256(res.getString("apk"));
        } else {
            return res.getString("key").toLowerCase();
        }
    }

    public void checkArgs() throws PaprikaArgException, IOException, NoSuchAlgorithmException {
        if (res.getString("key") != null) {
            String sha256 = computeSha256(res.getString("apk"));
            if (!sha256.equals(res.getString("key").toLowerCase())) {
                throw new PaprikaArgException("The given key is different from sha256 of the apk");
            }
        }
        if (!res.getString("date").matches(DATE_REGEX)) {
            throw new PaprikaArgException("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
        }
    }


}
