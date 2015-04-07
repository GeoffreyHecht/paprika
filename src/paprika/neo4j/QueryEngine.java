package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class QueryEngine {

    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private static int classClomplexity = 22;
    private static int numberofInterfaces= 3;
    private static int lcom = 15;
    private static int numberofMethods = 12;
    private static int numberofAttributes = 8;
    private static int numberofInstructions = 14;

    public String getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(String csvFile) {
        this.csvFile = csvFile;
    }

    private String csvFile;

    public QueryEngine(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
    }

    public void shutDown(){
        databaseManager.shutDown();
    }

   public void MIMQuery() throws CypherException, IOException {
       Result result;
       try (Transaction ignored = graphDatabaseService.beginTx()) {
           result = graphDatabaseService.execute("MATCH (m1:Method) WHERE NOT HAS(m1.`is_static`) AND NOT m1-[:USES]->(:Variable)  AND NOT (m1)-[:CALLS]->(:Method)  RETURN m1.app_key,count(m1)");
           resultToCSV(result);
       }
   }

    public void IGSQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (m1:Method)-[:CALLS]->(m2:Method),(cl:Class) WHERE (m2.is_setter OR m2.is_getter) AND cl-[:CLASS_OWNS_METHOD]->m1 AND cl-[:CLASS_OWNS_METHOD]->m2 RETURN m1.app_key,count(m1)");
            resultToCSV(result);
        }
    }

    public void LICQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE HAS(cl.is_inner_class) AND NOT HAS(cl.is_static) RETURN cl.app_key,count(cl)");
            resultToCSV(result);
        }
    }

    public void NLMRQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE HAS(cl.is_activity) AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method { name: 'onLowMemory' }) AND NOT cl-[:EXTENDS]->(:Class) RETURN cl.app_key,count(cl)");
            resultToCSV(result);
        }
    }


    public void CCQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE cl.class_complexity > "+ classClomplexity +" RETURN cl.app_key,count(cl)");
            resultToCSV(result);
        }
    }

    public void LMQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (m:Method) WHERE m.number_of_instructions >" + numberofInstructions + "RETURN m.app_key,count(m)");
            resultToCSV(result);
        }
    }

    public void SAKQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE cl.number_of_implemented_interfaces > " + numberofInterfaces + " RETURN cl.app_key,count(cl)");
            resultToCSV(result);
        }
    }

    public void GodClassQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE cl.lack_of_cohesion_in_methods >" + lcom + " AND cl.number_of_methods > " + numberofMethods + " AND cl.number_of_attributes > " + numberofAttributes + " RETURN cl.app_key,count(cl)");
            resultToCSV(result);
        }
    }

    public void AnalyzedAppQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (a:App) RETURN  a.app_key,a.package,a.version_name,a.date_analysis,a.number_of_classes");
            resultToCSV(result);
        }
    }

    public double calculateHighThreshold(String nodeType, String property){
        double res = 0;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:"+nodeType+") RETURN percentileDisc(n."+property+",0.25) as Q1, percentileDisc(n."+property+",0.75) as Q3";
            result = graphDatabaseService.execute(query);
            //Only one result in that case
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                int q1 = (int) row.get("Q1");
                int q3 = (int) row.get("Q3");
                res = q3 + ( 1.5 * ( q3 - q1));
            }
        }
        return res;
    }

    public void deleteQuery(String appKey) throws CypherException, IOException {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n {app_key: '"+appKey+"'})-[r]-() DELETE n,r");
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    public double calculateClassComplexityThreshold(){
        return calculateHighThreshold("Class", "class_complexity");
    }

    public double calculateNumberofInstructionsThreshold(){
        return calculateHighThreshold("Method","number_of_instructions");
    }

    public double calculateNumberOfImplementedInterfacesThreshold(){
        return calculateHighThreshold("Class","number_of_implemented_interfaces");
    }

    public double calculateLackofCohesionInMethodsThreshold(){
        return calculateHighThreshold("Class","lack_of_cohesion_in_methods");
    }

    public double calculateNumberOfMethodsThreshold(){
        return calculateHighThreshold("Class","number_of_methods");
    }

    public double calculateNumberOfAttributesThreshold(){
        return calculateHighThreshold("Class","number_of_attributes");
    }

    private void resultToCSV(Result result) throws IOException {
        FileWriter fw = new FileWriter(csvFile);
        BufferedWriter writer = new BufferedWriter( fw );
        List<String> columns = result.columns();
        Object val;
        for(String col : columns){
            writer.write(col);
            writer.write(',');
        }
        writer.newLine();
        while ( result.hasNext()){
            Map<String,Object> row = result.next();
            for(String col : columns){
                val = row.get(col);
                if(val != null){
                    writer.write(val.toString());
                    writer.write(',');
                }
            }
            writer.newLine();
        }
        writer.close();
        fw.close();
    }

    private void deleteExternalClasses(String appKey){
        deleteEntityOut(appKey, "ExternalClass", "ExternalMethod", "CLASS_OWNS_METHOD");
    }

    private void deleteExternalMethods(String appKey){
        deleteEntityIn(appKey, "ExternalMethod", "Method", "CALLS");
    }

    private void deleteCalls(String appKey){
        deleteRelations(appKey,"Method","Method","CALLS");
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
        deleteRelations(appKey,"Method","Variable","USES");
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
}
