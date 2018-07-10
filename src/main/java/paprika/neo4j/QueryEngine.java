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

package paprika.neo4j;

import net.sourceforge.argparse4j.inf.Namespace;
import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import paprika.neo4j.queries.PaprikaQuery;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class QueryEngine {

    protected GraphDatabaseService graphDatabaseService;
    protected DatabaseManager databaseManager;
    protected Namespace arg;

    protected String csvPrefix;

    public String getCsvPrefix() {
        return csvPrefix;
    }

    public void setCsvPrefix(String csvPrefix) {
        this.csvPrefix = csvPrefix;
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }

    public QueryEngine(String DatabasePath, Namespace arg) {
        this.databaseManager = new DatabaseManager(DatabasePath);
        this.arg = arg;
        databaseManager.start();
        graphDatabaseService = databaseManager.getGraphDatabaseService();
        csvPrefix = "";
    }

    public Namespace getArg() {
        return arg;
    }

    public void shutDown() {
        databaseManager.shutDown();
    }

    public void resultToCSV(Result result, String csvSuffix) throws IOException {
        String name = csvPrefix + csvSuffix;
        BufferedWriter writer = new BufferedWriter(new FileWriter(name));
        List<String> columns = result.columns();
        writeColumnLabels(columns, writer);
        while (result.hasNext()) {
            writeRowValues(result.next(), columns, writer);
        }
        writer.close();
    }

    private void writeColumnLabels(List<String> columns, BufferedWriter writer) throws IOException {
        for (int i = 0; i < columns.size() - 1; i++) {
            writer.write(columns.get(i));
            writer.write(',');
        }
        writer.write(columns.get(columns.size() - 1));
        writer.newLine();
    }

    private void writeRowValues(Map<String, Object> row, List<String> columns, BufferedWriter writer)
            throws IOException {
        Object val;
        for (int i = 0; i < columns.size() - 1; i++) {
            val = row.get(columns.get(i));
            if (val != null) {
                writer.write(val.toString());
                writer.write(',');
            }
        }
        val = row.get(columns.get(columns.size() - 1));
        if (val != null) {
            writer.write(val.toString());
        }
        writer.newLine();
    }

    public void fuzzyResultToCSV(List<Map<String, Object>> rows, List<String> columns, String csvSuffix)
            throws IOException {
        String name = csvPrefix + csvSuffix;
        BufferedWriter writer = new BufferedWriter(new FileWriter(name));
        writeColumnLabels(columns, writer);
        for (Map<String, Object> row : rows) {
            writeRowValues(row, columns, writer);
        }
        writer.close();
    }

    public void statsToCSV(Map<String, Double> stats, String csvSuffix) throws IOException {
        String name = csvPrefix + csvSuffix;
        FileWriter fw = new FileWriter(name);
        BufferedWriter writer = new BufferedWriter(fw);
        Set<String> keys = stats.keySet();
        for (String key : keys) {
            writer.write(key);
            writer.write(',');
        }
        writer.newLine();
        for (String key : keys) {
            writer.write(String.valueOf(stats.get(key)));
            writer.write(',');
        }
        writer.close();
        fw.close();
    }

    public void deleteQuery(String appKey) throws CypherException {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n {app_key: '" + appKey + "'})-[r]-() DELETE n,r");
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteExternalClasses(String appKey) {
        deleteEntityOut(appKey, "ExternalClass", "ExternalMethod", "CLASS_OWNS_METHOD");
    }

    private void deleteExternalMethods(String appKey) {
        deleteEntityIn(appKey, "ExternalMethod", "Method", "CALLS");
    }

    private void deleteCalls(String appKey) {
        deleteRelations(appKey, "Method", "Method", "CALLS");
    }

    private void deleteMethods(String appKey) {
        deleteEntityIn(appKey, "Method", "Class", "CLASS_OWNS_METHOD");
    }

    private void deleteClasses(String appKey) {
        deleteEntityIn(appKey, "Class", "App", "APP_OWNS_CLASS");
    }

    private void deleteVariables(String appKey) {
        deleteEntityIn(appKey, "Variable", "Class", "CLASS_OWNS_VARIABLE");
    }

    private void deleteArguments(String appKey) {
        deleteEntityIn(appKey, "Argument", "Method", "METHOD_OWNS_ARGUMENT");
    }

    private void deleteUses(String appKey) {
        deleteRelations(appKey, "Method", "Variable", "USES");
    }

    private void deleteImplements(String appKey) {
        deleteRelations(appKey, "Class", "Class", "IMPLEMENTS");
    }

    private void deleteExtends(String appKey) {
        deleteRelations(appKey, "Class", "Class", "EXTENDS");
    }

    private void deleteApp(String appKey) {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:App {app_key: '" + appKey + "'}) DELETE n";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteRelations(String appKey, String nodeType1, String nodeType2, String reltype) {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:" + nodeType1 + "  {app_key: '" + appKey + "'})-[r:" + reltype + "]->(m:" + nodeType2 + "{app_key: '" + appKey + "'}) DELETE r";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteEntityIn(String appKey, String nodeType1, String nodeType2, String reltype) {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:" + nodeType1 + " {app_key: '" + appKey + "'})<-[r:" + reltype + "]-(m:" + nodeType2 + "{app_key: '" + appKey + "'}) DELETE n,r";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteEntityOut(String appKey, String nodeType1, String nodeType2, String reltype) {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:" + nodeType1 + " {app_key: '" + appKey + "'})-[r:" + reltype + "]->(m:" + nodeType2 + "{app_key: '" + appKey + "'}) DELETE n,r";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    public void deleteEntireApp(String appKey) {
        //Delete have to be done in that order to ensure that relations are correctly deleted
        deleteExternalClasses(appKey);
        deleteExternalMethods(appKey);
        deleteUses(appKey);
        deleteCalls(appKey);
        deleteVariables(appKey);
        deleteArguments(appKey);
        deleteMethods(appKey);
        deleteImplements(appKey);
        deleteExtends(appKey);
        deleteClasses(appKey);
        deleteApp(appKey);
    }

    public List<String> findKeysFromPackageName(String appName) throws CypherException {
        ArrayList<String> keys = new ArrayList<>();
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute("MATCH (n:App) WHERE n.package='" + appName + "' RETURN n.app_key as key");
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                keys.add((String) row.get("key"));
            }
        }
        return keys;
    }

    public void deleteEntireAppFromPackage(String name) {
        System.out.println("Deleting app with package :" + name);
        List<String> keys = findKeysFromPackageName(name);
        for (String key : keys) {
            System.out.println("Deleting app with app_key :" + key);
            deleteEntireApp(key);
        }
    }

    public void executeRequest(String request) throws IOException {
        new PaprikaQuery("CUSTOM", this) {
            @Override
            public String getQuery(boolean details) {
                return request;
            }
        }.execute(false);
    }
}
