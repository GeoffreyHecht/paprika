package paprika;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import paprika.analyzer.Analyzer;
import paprika.analyzer.SootAnalyzer;
import paprika.neo4j.ModelToGraph;
import paprika.neo4j.QueryEngine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        Subparser queryParser = subparsers.addParser("query").help("Query the database");
        queryParser.addArgument("-db", "--database").required(true).help("Path to neo4J Database folder");
        queryParser.addArgument("-s", "--stats").help("Stats mode to get the thresholds values");
        queryParser.addArgument("-r", "--request").help("Request to execute");
        queryParser.addArgument("-c", "--csv").help("output csv file path").setDefault("result.csv");
        queryParser.addArgument("-k", "--key").help("key to delete");
        queryParser.addArgument("-p", "--package").help("Package of the applications to delete");
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
                arg.getDouble("rating"),arg.getString("nbDownload"),arg.getString("versionCode"),arg.getString("versionName"),arg.getString("sdkVersion"),arg.getString("targetSdkVersion"));
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
        if(arg.get("stats") != null){
            System.out.println("Class complexity : "+queryEngine.calculateClassComplexityThreshold());
            System.out.println("LCOM : "+queryEngine.calculateLackofCohesionInMethodsThreshold());
            System.out.println("Number of attributes : "+queryEngine.calculateNumberOfAttributesThreshold());
            System.out.println("Number of implemented interfaces : "+queryEngine.calculateNumberOfImplementedInterfacesThreshold());
            System.out.println("Number of Methods : "+queryEngine.calculateNumberOfMethodsThreshold());
            System.out.println("Number of Instructions : " + queryEngine.calculateNumberofInstructionsThreshold());
        }
        String request = arg.get("request");
        if(request != null){
            queryEngine.setCsvFile(arg.getString("csv"));
            System.out.println("Saving to file : "+arg.getString("csv"));
            if(request.equals("MIM")){
                queryEngine.MIMQuery();
            }else if(request.equals("IGS")){
                queryEngine.IGSQuery();
            }else if(request.equals("LIC")){
                queryEngine.LICQuery();
            }else if(request.equals("NLMR")){
                queryEngine.NLMRQuery();
            }else if(request.equals("CC")){
                queryEngine.CCQuery();
            }else if(request.equals("LM")){
                queryEngine.LMQuery();
            }else if(request.equals("SAK")){
                queryEngine.SAKQuery();
            }else if(request.equals("GOD")){
                queryEngine.GodClassQuery();
            }else if(request.equals("ANALYZED")){
                queryEngine.AnalyzedAppQuery();
            }else if(request.equals("DELETE")){
                queryEngine.deleteQuery(arg.getString("key"));
            }else if(request.equals("DELETEAPP")){
                if(arg.get("key") != null) { queryEngine.deleteEntireApp(arg.getString("key")); }
                else {
                    queryEngine.deleteEntireAppFromPackage(arg.getString("package"));
                }
            }
        }
        queryEngine.shutDown();
        System.out.println("Done");
    }
}