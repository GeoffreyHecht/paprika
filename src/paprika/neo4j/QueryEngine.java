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

    protected static double lcom = 22.5;
    protected static double numberofMethods = 14.5;
    protected static double numberofMethodsForInterfaces = 8.5;
    protected static double numberofAttributes = 7.5;
    protected static double numberofInstructions = 15;
    protected static double cyclomatic_complexity= 3.5;

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

    public QueryEngine(){
        csvPrefix = "";
    }

    public void shutDown(){
        databaseManager.shutDown();
    }

   public void MIMQuery() throws CypherException, IOException {
       Result result;
       try (Transaction ignored = graphDatabaseService.beginTx()) {
           result = graphDatabaseService.execute("MATCH (m1:Method) WHERE NOT HAS(m1.`is_static`) AND NOT m1-[:USES]->(:Variable)  AND NOT (m1)-[:CALLS]->(:Method) AND NOT HAS(m1.is_init)  RETURN m1.app_key as app_key,count(m1) as MIM");
           resultToCSV(result,"_MIM.csv");
       }
   }

    public void IGSQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (a:App) WITH a.app_key as key MATCH (cl:Class {app_key: key})-[:CLASS_OWNS_METHOD]->(m1:Method {app_key: key})-[:CALLS]->(m2:Method {app_key: key}) WHERE (m2.is_setter OR m2.is_getter) AND cl-[:CLASS_OWNS_METHOD]->m2 RETURN m1.app_key as app_key,count(m1) as IGS");
            resultToCSV(result,"_IGS.csv");
        }
    }

    public void LICQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE HAS(cl.is_inner_class) AND NOT HAS(cl.is_static) RETURN cl.app_key as app_key,count(cl) as LIC");
            resultToCSV(result,"_LIC.csv");
        }
    }

    public void NLMRQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE HAS(cl.is_activity) AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method { name: 'onLowMemory' }) AND NOT cl-[:EXTENDS]->(:Class) RETURN cl.app_key as app_key,count(cl) as NLMR");
            resultToCSV(result,"_NLMR.csv");
        }
    }




    public void LMQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (m:Method) WHERE m.number_of_instructions >" + numberofInstructions + " RETURN m.app_key as app_key,count(m) as LM");
            resultToCSV(result,"_LM.csv");
        }
    }

    public void SAKQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE HAS(cl.is_interface) AND cl.number_of_methods > " + numberofMethodsForInterfaces + " RETURN cl.app_key as app_key,count(cl) as SAK");
            resultToCSV(result,"_SAK.csv");
        }
    }

    public void BlobClassQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE cl.lack_of_cohesion_in_methods >" + lcom + " AND cl.number_of_methods > " + numberofMethods + " AND cl.number_of_attributes > " + numberofAttributes + " RETURN cl.app_key as app_key,count(cl) as BLOB");
            resultToCSV(result,"_BLOB.csv");
        }
    }

    public void OverdrawQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (:Class{parent_name:\"android.view.View\"})-[:CLASS_OWNS_METHOD]->(n:Method{name:\"onDraw\"})-[:METHOD_OWNS_ARGUMENT]->(:Argument{position:1,name:\"android.graphics.Canvas\"}) \n" +
                    "WHERE NOT n-[:CALLS]->(:ExternalMethod{full_name:\"clipRect#android.graphics.Canvas\"}) AND NOT n-[:CALLS]->(:ExternalMethod{full_name:\"quickReject#android.graphics.Canvas\"})\n" +
                    "RETURN n.app_key as app_key,count(n) as UIO");
            resultToCSV(result,"_UIO.csv");
        }
    }

    public void HeavyServiceStartQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (c:Class{is_service:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'}) WHERE m.number_of_instructions > "+numberofInstructions+" AND m.cyclomatic_complexity>"+cyclomatic_complexity+" return m.app_key as app_key,count(m) as HSS");
            resultToCSV(result,"_HSS.csv");
        }
    }

    public void HeavyBroadcastReceiverQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (c:Class{is_broadcast_receiver:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onReceive'}) WHERE m.number_of_instructions > "+numberofInstructions+" AND m.cyclomatic_complexity>"+cyclomatic_complexity+" return m.app_key as app_key,count(m) as HBR");
            resultToCSV(result,"_HBR.csv");
        }
    }

    public void HeavyASyncTaskStepsQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (c:Class{parent_name:'android.os.AsyncTask'})-[:CLASS_OWNS_METHOD]->(m:Method) WHERE (m.name='onPreExecute' OR m.name='onProgressUpdate' OR m.name='onPostExecute') AND  m.number_of_instructions >"+numberofInstructions+" AND m.cyclomatic_complexity > "+cyclomatic_complexity+" return m.app_key as app_key,count(m) as HAS");
            resultToCSV(result,"_HAS.csv");
        }
    }

    public void UnsuitedLRUCacheSizeQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("Match (m:Method)-[:CALLS]->(e:ExternalMethod {full_name:'<init>#android.util.LruCache'}) WHERE NOT (m)-[:CALLS]->(:ExternalMethod {full_name:'getMemoryClass#android.app.ActivityManager'}) return m.app_key as app_key,count(m) as UCS");
            resultToCSV(result,"_UCS.csv");
        }
    }

    public void InitOnDrawQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (:Class{parent_name:'android.view.View'})-[:CLASS_OWNS_METHOD]->(n:Method{name:'onDraw'})-[:CALLS]->({name:'<init>'}) return n.app_key as app_key,count(n) as IOD");
            resultToCSV(result,"_IOD.csv");
        }
    }

    public void UnsupportedHardwareAccelerationQuery() throws CypherException, IOException {
        Result result;
        String [] uhas = {
                "drawPicture#android.graphics.Canvas",
                "drawVertices#android.graphics.Canvas",
                "drawPosText#android.graphics.Canvas",
                "drawTextOnPath#android.graphics.Canvas",
                "drawPath#android.graphics.Canvas",
                "setLinearText#android.graphics.Paint",
                "setMaskFilter#android.graphics.Paint",
                "setPathEffect#android.graphics.Paint",
                "setRasterizer#android.graphics.Paint",
                "setSubpixelText#android.graphics.Paint"
        };
        String query = "MATCH (m:Method)-[:CALLS]->(e:ExternalMethod) WHERE e.full_name='"+uhas[0]+"'";
        for (int i=1; i < uhas.length;i++){
            query += " OR e.full_name='" + uhas[i] + "' ";
        }
        query += "return m.app_key, count(m) as UHA";
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute(query);
            resultToCSV(result,"_UHA.csv");
        }
    }

    public void HashMapUsage() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (m:Method)-[:CALLS]->(e:ExternalMethod{full_name:'<init>#java.util.HashMap'}) return m.app_key, count(m) as HMU");
            resultToCSV(result,"_HMU.csv");
        }
    }

    public void InvalidateWithoutRect() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (:Class{parent_name:\"android.view.View\"})-[:CLASS_OWNS_METHOD]->(n:Method{name:\"onDraw\"})-[:CALLS]->(e:ExternalMethod{name:'invalidate'}) WHERE NOT e-[:METHOD_OWNS_ARGUMENT]->(:ExternalArgument) return n.app_key, count(n) as IWR");
            resultToCSV(result,"_IWR.csv");
        }
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
            result = graphDatabaseService.execute("MATCH (n:Class) WHERE has(n.is_inner_class) return n.app_key as app_key,count(n) as nb_inner_classes");
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

    public void executeRequest(String request){
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            graphDatabaseService.execute(request);
        }
    }
}
