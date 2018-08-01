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
import paprika.DatabaseManager;
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.neo4j.queries.PaprikaQuery;
import paprika.query.neo4j.queries.QueryPropertiesReader;
import paprika.query.neo4j.queries.antipatterns.fuzzy.FuzzyQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static paprika.launcher.arg.Argument.DATABASE_ARG;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class QueryEngine {

    private static final String APP_NAMES_QUERY =
            "MATCH (n:App) RETURN n.app_key AS app_key, n.name AS app_name";

    private Map<String, String> keysToNames;

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
        executeAndWriteToCSV(query.getQuery(details), query.getCSVSuffix(), details);
    }

    public void executeAndWriteToCSV(String request, String suffix, boolean details) throws IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute(request);
            List<Map<String, Object>> rows = result.stream().map(HashMap::new).collect(Collectors.toList());
            List<String> columns = new ArrayList<>(result.columns());
            if (details) {
                addAppNamesToResult(rows, columns);
            }
            new CSVWriter(csvPrefix).resultToCSV(rows, columns, suffix);
        }
    }

    public void executeFuzzy(FuzzyQuery query, boolean details) throws IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute(query.getFuzzyQuery(details));
            List<Map<String, Object>> rows = result.stream().map(HashMap::new).collect(Collectors.toList());
            List<String> columns = new ArrayList<>(result.columns());
            if (details) {
                addAppNamesToResult(rows, columns);
            }
            columns.add("fuzzy_value");
            new CSVWriter(csvPrefix).resultToCSV(query.getFuzzyResult(rows, query.getFcl()),
                    columns, query.getFuzzySuffix());
        }
    }

    private void addAppNamesToResult(List<Map<String, Object>> rows, List<String> columns) {
        columns.add("app_name");
        if (keysToNames == null) {
            fillKeysToNames();
        }
        rows.forEach(row -> row.put("app_name", keysToNames.get(row.get("app_key").toString())));
    }

    private void fillKeysToNames() {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result namesResult = graphDatabaseService.execute(APP_NAMES_QUERY);
            keysToNames = namesResultToMap(namesResult.stream()
                    .collect(Collectors.toList()));
        }
    }

    private Map<String, String> namesResultToMap(List<Map<String, Object>> rows) {
        Map<String, String> result = new HashMap<>();
        rows.forEach(row -> result.put(row.get("app_key").toString(), row.get("app_name").toString()));
        return result;
    }

    public int executeAndCount(String request, String countLabel) {
        try (Transaction transaction = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute(request);
            transaction.success();
            return Integer.valueOf(result.next().get(countLabel).toString());
        }
    }

    public PaprikaArgParser getArgParser() {
        return arg;
    }

    public void shutDown() {
        databaseManager.shutDown();
    }

}
