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

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class QueryEngine {

    protected GraphDatabaseService graphDatabaseService;
    protected DatabaseManager databaseManager;

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


    public QueryEngine(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        graphDatabaseService = databaseManager.getGraphDatabaseService();
        csvPrefix = "";
    }

    public void shutDown(){
        databaseManager.shutDown();
    }

    public void AnalyzedAppQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (a:App) RETURN  a.app_key as app_key, a.category as category,a.package as package, a.version_code as version_code, a.date_analysis as date_analysis,a.number_of_classes as number_of_classes,a.size as size,a.rating as rating,a.nb_download as nb_download, a.number_of_methods as number_of_methods, a.number_of_activities as number_of_activities,a.number_of_services as number_of_services,a.number_of_interfaces as number_of_interfaces,a.number_of_abstract_classes as number_of_abstract_classes,a.number_of_broadcast_receivers as number_of_broadcast_receivers,a.number_of_content_providers as number_of_content_providers, a.number_of_variables as number_of_variables, a.number_of_views as number_of_views, a.number_of_inner_classes as number_of_inner_classes, a.number_of_async_tasks as number_of_async_tasks");
            resultToCSV(result,"_ANALYZED.csv");
        }
    }

    public void getPropertyForAllApk(String nodeType, String property,String suffix) throws IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:" + nodeType + ") RETURN n.app_key as app_key, n.name as name, n."+property+" as "+property;
            result = graphDatabaseService.execute(query);
            resultToCSV(result, suffix);
        }
    }

    public void getAllLCOM() throws IOException {
        getPropertyForAllApk("Class", "lack_of_cohesion_in_methods","_ALL_LCOM.csv");
    }

    public void getAllClassComplexity() throws IOException {
        getPropertyForAllApk("Class", "class_complexity","_ALL_CLASS_COMPLEXITY.csv");
    }

    public void getAllNumberOfMethods() throws IOException {
        getPropertyForAllApk("Class", "number_of_methods","_ALL_NUMBER_OF_METHODS.csv");
    }

    public void getAllCyclomaticComplexity() throws IOException {
        getPropertyForAllApk("Method", "cyclomatic_complexity","_ALL_CYCLOMATIC_COMPLEXITY.csv");
    }


    public void resultToCSV(Result result, String csvSuffix) throws IOException {
        String name = csvPrefix+csvSuffix;
        FileWriter fw = new FileWriter(name);
        BufferedWriter writer = new BufferedWriter( fw );
        List<String> columns = result.columns();
        Object val;
        int i;
        int columns_size = columns.size()-1;
        for(i=0;i<columns_size;i++){
            writer.write(columns.get(i));
            writer.write(',');
        }
        writer.write(columns.get(i));
        writer.newLine();
        while ( result.hasNext()){
            Map<String,Object> row = result.next();
            for(i=0;i<columns_size;i++){
                val = row.get(columns.get(i));
                if(val != null){
                    writer.write(val.toString());
                    writer.write(',');
                }
            }
            val = row.get(columns.get(i));
            if(val != null){
                writer.write(val.toString());
            }
            writer.newLine();
        }
        writer.close();
        fw.close();
    }

    public void resultToCSV(List<Map> rows,List<String> columns, String csvSuffix) throws IOException {
        String name = csvPrefix+csvSuffix;
        FileWriter fw = new FileWriter(name);
        BufferedWriter writer = new BufferedWriter( fw );
        Object val;
        int i;
        int columns_size = columns.size()-1;
        for(i=0;i<columns_size;i++){
            writer.write(columns.get(i));
            writer.write(',');
        }
        writer.write(columns.get(i));
        writer.newLine();
        for(Map<String,Object> row : rows){
            for(i=0;i<columns_size;i++){
                val = row.get(columns.get(i));
                if(val != null){
                    writer.write(val.toString());
                    writer.write(',');
                }
            }
            val = row.get(columns.get(i));
            if(val != null){
                writer.write(val.toString());
            }
            writer.newLine();
        }
        writer.close();
        fw.close();
    }
    public void statsToCSV(Map<String,Double> stats, String csvSuffix) throws IOException {
        String name = csvPrefix+csvSuffix;
        FileWriter fw = new FileWriter(name);
        BufferedWriter writer = new BufferedWriter( fw );
        Set<String> keys = stats.keySet();
        for(String key : keys){
            writer.write(key);
            writer.write(',');
        }
        writer.newLine();
        for(String key : keys){
            writer.write(String.valueOf(stats.get(key)));
            writer.write(',');
        }
        writer.close();
        fw.close();
    }

    public void deleteQuery(String appKey) throws CypherException, IOException {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n {app_key: '"+appKey+"'})-[r]-() DELETE n,r");
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteExternalClasses(String appKey){
        deleteEntityOut(appKey, "ExternalClass", "ExternalMethod", "CLASS_OWNS_METHOD");
    }

    private void deleteExternalMethods(String appKey){
        deleteEntityIn(appKey, "ExternalMethod", "Method", "CALLS");
    }

    private void deleteCalls(String appKey){
        deleteRelations(appKey, "Method", "Method", "CALLS");
    }

    private void deleteMethods(String appKey){
        deleteEntityIn(appKey, "Method", "Class", "CLASS_OWNS_METHOD");
    }

    private void deleteClasses(String appKey){
        deleteEntityIn(appKey, "Class", "App", "APP_OWNS_CLASS");
    }

    private void deleteVariables(String appKey){
        deleteEntityIn(appKey, "Variable", "Class", "CLASS_OWNS_VARIABLE");
    }

    private void deleteArguments(String appKey){
        deleteEntityIn(appKey, "Argument", "Method", "METHOD_OWNS_ARGUMENT");
    }

    private void deleteUses(String appKey){
        deleteRelations(appKey, "Method", "Variable", "USES");
    }

    private void deleteImplements(String appKey){
        deleteRelations(appKey,"Class","Class","IMPLEMENTS");
    }

    private void deleteExtends(String appKey){
        deleteRelations(appKey,"Class","Class","EXTENDS");
    }

    private void deleteApp(String appKey){
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:App {app_key: '"+appKey+"'}) DELETE n";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteRelations(String appKey,String nodeType1,String nodeType2,String reltype){
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:"+nodeType1+"  {app_key: '"+appKey+"'})-[r:"+reltype+"]->(m:"+nodeType2+"{app_key: '"+appKey+"'}) DELETE r";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteEntityIn(String appKey,String nodeType1,String nodeType2,String reltype){
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:"+nodeType1+" {app_key: '"+appKey+"'})<-[r:"+reltype+"]-(m:"+nodeType2+"{app_key: '"+appKey+"'}) DELETE n,r";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    private void deleteEntityOut(String appKey,String nodeType1,String nodeType2,String reltype){
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            String request = "MATCH (n:"+nodeType1+" {app_key: '"+appKey+"'})-[r:"+reltype+"]->(m:"+nodeType2+"{app_key: '"+appKey+"'}) DELETE n,r";
            System.out.println(request);
            result = graphDatabaseService.execute(request);
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    public void deleteEntireApp(String appKey){
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

    public List<String> findKeysFromPackageName(String appName) throws CypherException, IOException {
        ArrayList<String> keys = new ArrayList<>();
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute("MATCH (n:App) WHERE n.package='"+appName+"' RETURN n.app_key as key");
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                keys.add((String) row.get("key"));
            }
        }
        return keys;
    }

    public void deleteEntireAppFromPackage(String name) throws IOException {
        System.out.println("Deleting app with package :"+name);
        List<String> keys = findKeysFromPackageName(name);
        for(String key : keys){
            System.out.println("Deleting app with app_key :"+key);
            deleteEntireApp(key);
        }
    }

    public void countVariables() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Variable) return n.app_key as app_key, count(n) as nb_variables");
            resultToCSV(result,"_COUNT_VARIABLE.csv");
        }
    }

    public void countInnerClasses() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Class) WHERE exists(n.is_inner_class) return n.app_key as app_key,count(n) as nb_inner_classes");
            resultToCSV(result,"_COUNT_INNER.csv");
        }
    }

    public void countAsyncClasses() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Class{parent_name:'android.os.AsyncTask'}) return n.app_key as app_key,count(n) as number_of_async");
            resultToCSV(result,"_COUNT_ASYNC.csv");
        }
    }

    public void countViews() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Class{parent_name:'android.view.View'}) return n.app_key as app_key,count(n) as number_of_views");
            resultToCSV(result,"_COUNT_VIEWS.csv");
        }
    }

    public void executeRequest(String request)  throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute(request);
            resultToCSV(result,"_CUSTOM.csv");
        }
    }
}
