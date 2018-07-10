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

package paprika.neo4j.queries;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import paprika.commands.PaprikaCommand;
import paprika.neo4j.QueryEngine;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 17/08/15.
 */
public abstract class PaprikaQuery implements PaprikaCommand {

    protected String queryName;
    protected QueryEngine queryEngine;
    protected GraphDatabaseService graphDatabaseService;

    public PaprikaQuery(String queryName, QueryEngine queryEngine) {
        this.queryName = queryName;
        this.queryEngine = queryEngine;
        graphDatabaseService = queryEngine.getGraphDatabaseService();
    }

    public abstract String getQuery(boolean details);

    public void execute(boolean details) throws IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = getQuery(details);
            Result result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, getCSVSuffix());
        }
    }

    protected String getCSVSuffix() {
        return "_" + queryName + ".csv";
    }

    @Override
    public void run(boolean details) throws IOException {
        execute(details);
    }

}
