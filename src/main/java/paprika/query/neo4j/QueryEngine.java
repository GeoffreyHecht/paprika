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

package paprika.query.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.neo4j.queries.PaprikaQuery;
import paprika.query.neo4j.queries.QueryPropertiesReader;
import paprika.query.neo4j.queries.antipatterns.fuzzy.FuzzyQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static paprika.launcher.arg.Argument.DATABASE_ARG;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class QueryEngine {

    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private PaprikaArgParser arg;
    private String csvPrefix;
    private QueryPropertiesReader propsReader;

    public QueryEngine(PaprikaArgParser arg, QueryPropertiesReader reader) {
        this.databaseManager = new DatabaseManager(arg.getArg(DATABASE_ARG));
        this.arg = arg;
        this.propsReader = reader;
        databaseManager.start();
        graphDatabaseService = databaseManager.getGraphDatabaseService();
        csvPrefix = "";
    }

    public String getCsvPrefix() {
        return csvPrefix;
    }

    public void setCsvPrefix(String csvPrefix) {
        this.csvPrefix = csvPrefix;
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }

    public QueryPropertiesReader getPropsReader() {
        return propsReader;
    }

    public void execute(PaprikaQuery query, boolean details) throws IOException {
        executeAndWriteToCSV(query.getQuery(details), query.getCSVSuffix());
    }

    public void executeAndWriteToCSV(String request, String suffix) throws IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute(request);
            new CSVWriter(csvPrefix).resultToCSV(result, suffix);
        }
    }

    public int executeAndCountAll(String request, String countLabel) {
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute(request);
            transaction.success();
            return Integer.valueOf(result.next().get(countLabel).toString());
        }
    }

    public void executeFuzzy(FuzzyQuery query, boolean details) throws IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute(query.getFuzzyQuery(details));
            List<String> columns = new ArrayList<>(result.columns());
            columns.add("fuzzy_value");
            new CSVWriter(csvPrefix).fuzzyResultToCSV(query.getFuzzyResult(result, query.getFcl()),
                    columns, query.getFuzzySuffix());
        }
    }

    public PaprikaArgParser getArgParser() {
        return arg;
    }

    public void shutDown() {
        databaseManager.shutDown();
    }

}
