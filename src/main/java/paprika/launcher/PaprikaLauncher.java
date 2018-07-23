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

package paprika.launcher;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import paprika.analyzer.Analyzer;
import paprika.analyzer.SootAnalyzer;
import paprika.commands.CustomCommand;
import paprika.commands.PaprikaRequest;
import paprika.entities.PaprikaApp;
import paprika.neo4j.ModelToGraph;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.QueryPropertiesReader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static paprika.launcher.Argument.*;

public class PaprikaLauncher {

    private static final int SOOT_RETRIES = 3;

    private PaprikaArgParser argParser;
    private PrintStream out;
    private List<String> appsPaths;
    private boolean folderMode = false;

    public PaprikaLauncher(String[] args, PrintStream out) throws NoSuchAlgorithmException,
            IOException, PaprikaArgException {
        this.out = out;
        this.argParser = new PaprikaArgParser();
        try {
            argParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argParser.handleError(e);
        }
    }

    public void startPaprika() throws IOException, NoSuchAlgorithmException, PropertiesException {
        if (argParser.isAnalyseMode()) {
            analyseMode();
        } else if (argParser.isQueryMode()) {
            queryMode();
        }
    }

    private void analyseMode() throws IOException, NoSuchAlgorithmException, PropertiesException {
        boolean folderMode = argParser.isFolderMode();
        ModelToGraph modelToGraph = new ModelToGraph(argParser.getArg(DATABASE_ARG));
        List<String> appsPaths = argParser.getAppsPaths();
        ApkPropertiesParser propsParser = null;
        if (folderMode) {
            out.println("Analyzing all apps in folder " + argParser.getArg(APK_ARG));
            propsParser = new ApkPropertiesParser(out, argParser.getArg(APK_ARG) + "/"
                    + ApkPropertiesParser.PROPS_FILENAME, argParser.getAppsFilenames(appsPaths));
            propsParser.readProperties();
        }
        for (String apk : appsPaths) {
            out.println("Analyzing " + apk);
            PaprikaApp app = analyzeApp(apk, folderMode, 0, propsParser);
            saveIntoDatabase(app, modelToGraph);
        }
        out.println("Done");
    }

    public PaprikaApp analyzeApp(String apkPath, boolean folderMode, int retries,
                                 @Nullable ApkPropertiesParser propsParser)
            throws IOException, NoSuchAlgorithmException {
        out.println("Collecting metrics");
        Analyzer analyzer = new SootAnalyzer(apkPath, argParser.getArg(ANDROID_JARS_ARG));
        PaprikaAppCreator creator = new PaprikaAppCreator(argParser, apkPath);
        analyzer.prepareSoot();
        creator.readAppInfo();
        creator.fetchMissingAppInfo();
        if (propsParser != null) {
            creator.addApkProperties(propsParser);
        }
        try {
            analyzer.runAnalysis(creator.createApp(), argParser.getFlagArg(ONLY_MAIN_PACKAGE_ARG));
        } catch (RuntimeException e) {
            if (retries < SOOT_RETRIES) {
                // Soot, please stop crashing randomly. We'll try this again.
                out.println("Encountered soot issue on app " + apkPath);
                out.println("Restarting soot analysis...");
                return analyzeApp(apkPath, folderMode, retries + 1, propsParser);
            } else {
                e.printStackTrace(out);
                return null;
            }
        }
        return analyzer.getPaprikaApp();
    }

    public void saveIntoDatabase(PaprikaApp app, ModelToGraph modelToGraph) {
        out.println("Saving into database " + argParser.getArg(DATABASE_ARG));
        modelToGraph.insertApp(app);
    }


    public void queryMode() throws IOException {
        out.println("Executing Queries");
        QueryEngine queryEngine = new QueryEngine(argParser);
        QueryPropertiesReader.loadProperties(argParser.getArg(THRESHOLDS_ARG));
        String request = argParser.getArg(REQUEST_ARG);
        Boolean details = argParser.getFlagArg(DETAILS_ARG);
        String csvPrefix = getCSVPrefix(argParser.getArg(CSV_ARG));
        out.println("Resulting csv file name will start with prefix " + csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        PaprikaRequest paprikaRequest = PaprikaRequest.getRequest(request);
        if (paprikaRequest != null) {
            paprikaRequest.getCommand(queryEngine).run(details);
        } else {
            out.println("Executing custom request");
            new CustomCommand(queryEngine, request).run(false);
        }
        queryEngine.shutDown();
        out.println("Done");
    }

    private String getCSVPrefix(String csvPath) {
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR)) + "_" +
                String.valueOf(cal.get(Calendar.MONTH) + 1) + "_" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) +
                "_" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(cal.get(Calendar.MINUTE));
        return csvPath + csvDate;
    }

}
