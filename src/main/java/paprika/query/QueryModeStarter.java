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

package paprika.query;

import paprika.launcher.PaprikaStarter;
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.commands.CustomCommand;
import paprika.query.commands.PaprikaRequest;
import paprika.query.neo4j.QueryEngine;
import paprika.query.neo4j.queries.QueryPropertiesException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static paprika.launcher.arg.Argument.*;

public class QueryModeStarter extends PaprikaStarter {

    public QueryModeStarter(PaprikaArgParser argParser, PrintStream out) {
        super(argParser, out);
    }

    @Override
    public void start() {
        try {
            if (!new File(argParser.getArg(DATABASE_ARG)).exists()) {
                out.println("No database was found on the given path.");
            }
            out.println("Executing Queries");
            QueryEngine queryEngine = createQueryEngine();
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
        } catch (IOException | QueryPropertiesException e) {
            e.printStackTrace(out);
        }
    }

    private String getCSVPrefix(String csvPath) {
        Calendar cal = new GregorianCalendar();
        String csvDate = String.valueOf(cal.get(Calendar.YEAR)) + "_" +
                String.valueOf(cal.get(Calendar.MONTH) + 1) + "_" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) +
                "_" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(cal.get(Calendar.MINUTE));
        return csvPath + csvDate;
    }

}
