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

import paprika.launcher.PaprikaStarter;
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.neo4j.QueryEngine;
import paprika.query.neo4j.queries.QueryPropertiesException;

import java.io.IOException;
import java.io.PrintStream;

import static paprika.launcher.arg.Argument.DELETE_KEY;

public class DeleteModeStarter extends PaprikaStarter {

    private static final int BATCH_SIZE = 10000;

    public DeleteModeStarter(PaprikaArgParser argParser, PrintStream out) {
        super(argParser, out);
    }

    @Override
    public void start() {
        try {
            QueryEngine engine = createQueryEngine();
            String deleteQuery = "MATCH (n) WHERE n.app_key='" + argParser.getArg(DELETE_KEY) + "'\n" +
                    "WITH n LIMIT " + BATCH_SIZE + "\n" +
                    "DETACH DELETE n \n" +
                    "RETURN count(n)";
            out.println("Deleting nodes...");
            int count = 1;
            while (count != 0) {
                count = engine.executeAndCountAll(deleteQuery, "count(n)");
                printProgress();
            }
            out.println();
            out.println("Done.");
        } catch (IOException | QueryPropertiesException e) {
            e.printStackTrace(out);
        }
    }

    private int charCount = 1;

    private void printProgress() {
        if (charCount == 70) {
            out.print("\n");
            charCount = 0;
        } else {
            out.print(".");
            charCount++;
        }
    }
}
