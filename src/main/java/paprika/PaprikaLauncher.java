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

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import paprika.analyzer.Analyzer;
import paprika.analyzer.SootAnalyzer;
import paprika.commands.CustomCommand;
import paprika.commands.PaprikaRequest;
import paprika.entities.PaprikaApp;
import paprika.neo4j.ModelToGraph;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.QueryPropertiesReader;

import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;

import static paprika.PaprikaArgParser.*;

public class PaprikaLauncher {

    private PaprikaArgParser parser;
    private Namespace arg;
    private PrintStream print;

    public PaprikaLauncher(String[] args, PrintStream print) throws NoSuchAlgorithmException,
            IOException, PaprikaArgException {
        this.print = print;
        this.parser = new PaprikaArgParser();
        try {
            parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        arg = parser.getArguments();
    }

    public void startPaprika() throws IOException, NoSuchAlgorithmException {
        if (parser.isAnalyseMode()) {
            PaprikaApp app;
            try {
                app = analyzeApp();
            } catch (NoSuchElementException e) {
                // Soot, please stop crashing randomly. We'll try this again.
                print.println("Encountered soot issue on app " + arg.getString(APK_ARG));
                print.println("Restarting soot analysis...");
                app = analyzeApp();
            }
            saveIntoDatabase(app);
        } else if (parser.isQueryMode()) {
            queryMode();
        }
    }

    public PaprikaApp analyzeApp() throws IOException, NoSuchAlgorithmException {
        print.println("Collecting metrics");
        Namespace arg = parser.getArguments();
        Analyzer analyzer = new SootAnalyzer(arg.getString(APK_ARG), arg.getString(ANDROID_JARS_ARG),
                arg.getString(NAME_ARG), parser.getSha(),
                arg.getString(PACKAGE_ARG), arg.getString(DATE_ARG), arg.getInt(SIZE_ARG),
                arg.getString(DEVELOPER_ARG), arg.getString(CATEGORY_ARG), arg.getString(PRICE_ARG),
                arg.getDouble(RATING_ARG), arg.getInt(NB_DOWNLOAD_ARG), arg.getString(VERSION_CODE_ARG),
                arg.getString(VERSION_NAME_ARG), arg.getInt(SDK_VERSION_ARG), arg.getInt(TARGET_SDK_VERSION_ARG),
                arg.getBoolean(ONLY_MAIN_PACKAGE_ARG) != null);
        analyzer.prepareSoot();
        analyzer.runAnalysis();
        return analyzer.getPaprikaApp();
    }

    public void saveIntoDatabase(PaprikaApp app) {
        print.println("Saving into database " + arg.getString(DATABASE_ARG));
        ModelToGraph modelToGraph = new ModelToGraph(arg.getString(DATABASE_ARG));
        modelToGraph.insertApp(app);
        print.println("Done");
    }


    public void queryMode() throws IOException {
        print.println("Executing Queries");
        Namespace arg = parser.getArguments();
        QueryEngine queryEngine = new QueryEngine(arg.getString(DATABASE_ARG), arg);
        QueryPropertiesReader.loadProperties(arg.getString(THRESHOLDS_ARG));
        String request = arg.get(REQUEST_ARG);
        Boolean details = arg.get(DETAILS_ARG);
        String csvPrefix = getCSVPrefix(arg.getString(CSV_ARG));
        print.println("Resulting csv file name will start with prefix " + csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        PaprikaRequest paprikaRequest = PaprikaRequest.getRequest(request);
        if (paprikaRequest != null) {
            paprikaRequest.getCommand(queryEngine).run(details);
        } else {
            print.println("Executing custom request");
            new CustomCommand(queryEngine, request).run(false);
        }
        queryEngine.shutDown();
        print.println("Done");
    }

    private String getCSVPrefix(String csvPath) {
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR)) + "_" +
                String.valueOf(cal.get(Calendar.MONTH) + 1) + "_" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) +
                "_" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(cal.get(Calendar.MINUTE));
        return csvPath + csvDate;
    }

}
