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
import net.sourceforge.argparse4j.inf.*;
import paprika.analyzer.Analyzer;
import paprika.analyzer.SootAnalyzer;
import paprika.neo4j.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

/**
 * Created by Geoffrey Hecht on 19/05/14.
 */

public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

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
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashValue.length; i++) {
            sb.append(Integer.toString((hashValue[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
        Subparsers subparsers = parser.addSubparsers().dest("sub_command");;
        Subparser analyseParser = subparsers.addParser("analyse").help("Analyse an app");
        analyseParser.addArgument("apk").help("Path of the APK to analyze");
        analyseParser.addArgument("-a", "--androidJars").required(true).help("Path to android platforms jars");
        analyseParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        analyseParser.addArgument("-n", "--name").required(true).help("Name of the application");
        analyseParser.addArgument("-p", "--package").required(true).help("Application main package");
        analyseParser.addArgument("-k", "--key").required(true).help("sha256 of the apk used as identifier");
        analyseParser.addArgument("-dev", "--developer").required(true).help("Application developer");
        analyseParser.addArgument("-cat", "--category").required(true).help("Application category");
        analyseParser.addArgument("-nd", "--nbDownload").required(true).help("Numbers of downloads for the app");
        analyseParser.addArgument("-d", "--date").required(true).help("Date of download");
        analyseParser.addArgument("-r", "--rating").type(Double.class).required(true).help("application rating");
        analyseParser.addArgument("-pr", "--price").setDefault("Free").help("Price of the application");
        analyseParser.addArgument("-s", "--size").type(Integer.class).required(true).help("Size of the application");
        analyseParser.addArgument("-u", "--unsafe").help("Unsafe mode (no args checking)");
        analyseParser.addArgument("-vc", "--versionCode").setDefault("").help("Version Code of the application (extract from manifest)");
        analyseParser.addArgument("-vn", "--versionName").setDefault("").help("Version Name of the application (extract from manifest)");
        analyseParser.addArgument("-tsdk", "--targetSdkVersion").setDefault("").help("Target SDK Version (extract from manifest)");
        analyseParser.addArgument("-sdk", "--sdkVersion").setDefault("").help("sdk version (extract from manifest)");
        analyseParser.addArgument("-omp", "--onlyMainPackage").type(Boolean.class).setDefault(false).help("Analyze only the main package of the application");

        Subparser queryParser = subparsers.addParser("query").help("Query the database");
        queryParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        queryParser.addArgument("-r", "--request").help("Request to execute");
        queryParser.addArgument("-c", "--csv").help("path to register csv files").setDefault("");
        queryParser.addArgument("-dk", "--delKey").help("key to delete");
        queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
        queryParser.addArgument("-d", "--details").type(Boolean.class).setDefault(false).help("Show the concerned entity in the results");
        try {
            Namespace res = parser.parseArgs(args);
            if(res.getString("sub_command").equals("analyse")){
                runAnalysis(res);
            }
            else if(res.getString("sub_command").equals("query")){
                queryMode(res);
            }
        } catch (ArgumentParserException e) {
            analyseParser.handleError(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkArgs(Namespace arg) throws Exception{
        String sha256 = computeSha256(arg.getString("apk"));
        if(!sha256.equals(arg.getString("key").toLowerCase())){
            throw new Exception("The given key is different from sha256 of the apk");
        }
        if(!arg.getString("date").matches("^([0-9]{4})-([0-1][0-9])-([0-3][0-9])\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]*)$")){
            throw new Exception("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
        }
    }

    public static void runAnalysis(Namespace arg) throws Exception {
        System.out.println("Collecting metrics");
        if(arg.get("unsafe") == null){
            checkArgs(arg);
        }
        Analyzer analyzer = new SootAnalyzer(arg.getString("apk"),arg.getString("androidJars"),
                arg.getString("name"),arg.getString("key").toLowerCase(),
                arg.getString("package"),arg.getString("date"),arg.getInt("size"),
                arg.getString("developer"),arg.getString("category"),arg.getString("price"),
                arg.getDouble("rating"),arg.getString("nbDownload"),arg.getString("versionCode"),arg.getString("versionName"),arg.getString("sdkVersion"),arg.getString("targetSdkVersion"), arg.getBoolean("onlyMainPackage"));
        analyzer.init();
        analyzer.runAnalysis();
        System.out.println("Saving into database "+arg.getString("database"));
        ModelToGraph modelToGraph = new ModelToGraph(arg.getString("database"));
        modelToGraph.insertApp(analyzer.getPaprikaApp());
        System.out.println("Done");
    }

    public static void queryMode(Namespace arg) throws Exception {
        System.out.println("Executing Queries");
        QueryEngine queryEngine = new QueryEngine(arg.getString("database"));
        String request = arg.get("request");
        Boolean details = arg.get("details");
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR))+"_"+String.valueOf(cal.get(Calendar.MONTH)+1)+"_"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH))+"_"+String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+"_"+String.valueOf(cal.get(Calendar.MINUTE));
        String csvPrefix = arg.getString("csv")+csvDate;
        System.out.println("Resulting csv file name will start with prefix "+csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        switch(request){
            case "ARGB8888":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
            case "MIM":
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                break;
            case "IGS":
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                break;
            case "LIC":
                LICQuery.createLICQuery(queryEngine).execute(details);
                break;
            case "NLMR":
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                break;
            case "CC":
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                break;
            case "LM":
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                break;
            case "SAK":
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                break;
            case "BLOB":
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                break;
            case "OVERDRAW":
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                break;
            case "HSS":
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                break;
            case "HBR":
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                break;
            case "HAS":
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "THI":
                TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
                break;
            case "ALLHEAVY":
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "ANALYZED":
                queryEngine.AnalyzedAppQuery();
                break;
            case "DELETE":
                queryEngine.deleteQuery(arg.getString("delKey"));
                break;
            case "DELETEAPP":
                if(arg.get("delKey") != null) { queryEngine.deleteEntireApp(arg.getString("delKey")); }
                else {
                    queryEngine.deleteEntireAppFromPackage(arg.getString("delPackage"));
                }
                break;
            case "STATS":
                QuartileCalculator quartileCalculator = new QuartileCalculator(queryEngine);
                quartileCalculator.calculateClassComplexityQuartile();
                quartileCalculator.calculateLackofCohesionInMethodsQuartile();
                quartileCalculator.calculateNumberOfAttributesQuartile();
                quartileCalculator.calculateNumberOfImplementedInterfacesQuartile();
                quartileCalculator.calculateNumberOfMethodsQuartile();
                quartileCalculator.calculateNumberofInstructionsQuartile();
                quartileCalculator.calculateCyclomaticComplexityQuartile();
                quartileCalculator.calculateNumberOfMethodsForInterfacesQuartile();
                break;
            case "ALLLCOM":
                queryEngine.getAllLCOM();
                break;
            case "ALLCYCLO":
                queryEngine.getAllCyclomaticComplexity();
                break;
            case "ALLCC":
                queryEngine.getAllClassComplexity();
                break;
            case "ALLNUMMETHODS":
                queryEngine.getAllNumberOfMethods();
                break;
            case "COUNTVAR":
                queryEngine.countVariables();
                break;
            case "COUNTINNER":
                queryEngine.countInnerClasses();
                break;
            case "COUNTASYNC":
                queryEngine.countAsyncClasses();
                break;
            case "COUNTVIEWS":
                queryEngine.countViews();
                break;
            case "NONFUZZY":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                LICQuery.createLICQuery(queryEngine).execute(details);
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
                InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
                UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine).execute(details);
                HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
                InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
                break;
            case "FUZZY":
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                break;
            case "ALLAP":
                ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
                CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
                LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
                SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
                BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
                MIMQuery.createMIMQuery(queryEngine).execute(details);
                IGSQuery.createIGSQuery(queryEngine).execute(details);
                LICQuery.createLICQuery(queryEngine).execute(details);
                NLMRQuery.createNLMRQuery(queryEngine).execute(details);
                OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
                UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
                InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
                UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine).execute(details);
                HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
                InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
                TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
                break;
            case "FORCENOFUZZY":
                CCQuery.createCCQuery(queryEngine).execute(details);
                LMQuery.createLMQuery(queryEngine).execute(details);
                SAKQuery.createSAKQuery(queryEngine).execute(details);
                BLOBQuery.createBLOBQuery(queryEngine).execute(details);
                HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).execute(details);
                HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).execute(details);
                HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).execute(details);
                break;
            default:
                System.out.println("Executing custom request");
                queryEngine.executeRequest(request);
        }
        queryEngine.shutDown();
        System.out.println("Done");
    }
}