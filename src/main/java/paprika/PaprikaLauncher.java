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
import paprika.neo4j.ModelToGraph;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.QueryPropertiesReader;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static paprika.PaprikaArgParser.*;

public class PaprikaLauncher {

    public void startPaprika(String[] args) {
        PaprikaArgParser parser = new PaprikaArgParser();
        try {
            parser.parseArgs(args);
            if (parser.isAnalyseMode()) {
                runAnalysis(parser);
            } else if (parser.isQueryMode()) {
                queryMode(parser);
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runAnalysis(PaprikaArgParser parser) throws Exception {
        System.out.println("Collecting metrics");
        Namespace arg = parser.getArguments();
        Analyzer analyzer = new SootAnalyzer(arg.getString(APK_ARG), arg.getString(ANDROID_JARS_ARG),
                arg.getString(NAME_ARG), parser.getSha(),
                arg.getString(PACKAGE_ARG), arg.getString(DATE_ARG), arg.getInt(SIZE_ARG),
                arg.getString(DEVELOPER_ARG), arg.getString(CATEGORY_ARG), arg.getString(PRICE_ARG),
                arg.getDouble(RATING_ARG), arg.getString(NB_DOWNLOAD_ARG), arg.getString(VERSION_CODE_ARG),
                arg.getString(VERSION_NAME_ARG), arg.getInt(SDK_VERSION_ARG), arg.getInt(TARGET_SDK_VERSION_ARG),
                arg.getBoolean(ONLY_MAIN_PACKAGE_ARG) != null);
        analyzer.prepareSoot();
        analyzer.runAnalysis();
        System.out.println("Saving into database " + arg.getString(DATABASE_ARG));
        ModelToGraph modelToGraph = new ModelToGraph(arg.getString(DATABASE_ARG));
        modelToGraph.insertApp(analyzer.getPaprikaApp());
        System.out.println("Done");
    }

    private void queryMode(PaprikaArgParser parser) throws Exception {
        System.out.println("Executing Queries");
        Namespace arg = parser.getArguments();
        QueryEngine queryEngine = new QueryEngine(arg.getString(DATABASE_ARG), arg);
        QueryPropertiesReader.loadProperties(arg.getString(THRESHOLDS_ARG));
        String request = arg.get(REQUEST_ARG);
        Boolean details = arg.get(DETAILS_ARG);
        String csvPrefix = getCSVPrefix(arg.getString(CSV_ARG));
        System.out.println("Resulting csv file name will start with prefix " + csvPrefix);
        queryEngine.setCsvPrefix(csvPrefix);
        PaprikaRequest paprikaRequest = PaprikaRequest.getRequest(request);
        if (paprikaRequest != null) {
            paprikaRequest.getCommand(queryEngine).run(details);
        } else {
            System.out.println("Executing custom request");
            new CustomCommand(queryEngine, request).run(false);
        }
        queryEngine.shutDown();
        System.out.println("Done");
    }

    private String getCSVPrefix(String csvPath) {
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR)) + "_" +
                String.valueOf(cal.get(Calendar.MONTH) + 1) + "_" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) +
                "_" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(cal.get(Calendar.MINUTE));
        return  csvPath + csvDate;
    }

}
