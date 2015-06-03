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

    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private static double classClomplexity = 25;
    private static double numberofInterfaces= 5;
    private static double lcom = 20;
    private static double numberofMethods = 15;
    private static double numberofMethodsForInterfaces = 1;
    private static double numberofAttributes = 8;
    private static double numberofInstructions = 15;
    private static double cyclomatic_complexity= 3.5;

    private String csvPrefix;

    public String getCsvPrefix() {
        return csvPrefix;
    }

    public void setCsvPrefix(String csvPrefix) {
        this.csvPrefix = csvPrefix;
    }


    public QueryEngine(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        csvPrefix = "";
    }

    public void shutDown(){
        databaseManager.shutDown();
    }

   public void MIMQuery() throws CypherException, IOException {
       Result result;
       try (Transaction ignored = graphDatabaseService.beginTx()) {
           result = graphDatabaseService.execute("MATCH (m1:Method) WHERE NOT HAS(m1.`is_static`) AND NOT m1-[:USES]->(:Variable)  AND NOT (m1)-[:CALLS]->(:Method)  RETURN m1.app_key as app_key,count(m1) as MIM");
           resultToCSV(result,"_MIM.csv");
       }
   }

    public void IGSQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class)-[:CLASS_OWNS_METHOD]->(m1:Method)-[:CALLS]->(m2:Method) WHERE (m2.is_setter OR m2.is_getter) AND cl-[:CLASS_OWNS_METHOD]->m2 RETURN m1.app_key as app_key,count(m1) as IGS");
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


    public void CCQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE cl.class_complexity > "+ classClomplexity +" RETURN cl.app_key as app_key,count(cl) as CC");
            resultToCSV(result,"_CC.csv");
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

    public void AnalyzedAppQuery() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (a:App) RETURN  a.app_key,a.category,a.package,a.version_code,a.date_analysis,a.number_of_classes,a.size,a.rating,a.nb_download,a.sdk,a.target_sdk,a.number_of_activities,a.number_of_services,a.number_of_interfaces,a.number_of_abstract_classes,a.number_of_broadcast_receivers,a.number_of_content_providers");
            resultToCSV(result,"_ANALYZED.csv");
        }
    }

    public Map calculateQuartile(String nodeType, String property){
        Map<String, Double> res = new HashMap<>();
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:"+nodeType+") RETURN percentileDisc(n."+property+",0.25) as Q1,percentileDisc(n."+property+",0.5) as MED, percentileDisc(n."+property+",0.75) as Q3";
            result = graphDatabaseService.execute(query);
            //Only one result in that case
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                double q1 = (int) row.get("Q1");
                double med = (int) row.get("MED");
                double q3 = (int) row.get("Q3");
                double threshold  = q3 + ( 1.5 * ( q3 - q1));
                res.put("Q1",q1);
                res.put("Q3",q3);
                res.put("MED",med);
                res.put("THRESHOLD",threshold);
            }
        }
        return res;
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

    public Result calculateQuartilePerApk(String nodeType, String property){
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:" + nodeType + ") RETURN n.app_key as app_key, percentileDisc(n." + property + ",0.25) as Q1,percentileDisc(n." + property + ",0.5) as MED, percentileDisc(n." + property + ",0.75) as Q3";
            result = graphDatabaseService.execute(query);
        }
        return result;
    }

    public void calculateLCOMQuartilePerAPK() throws IOException {
        resultToCSV(calculateQuartilePerApk("Class", "lack_of_cohesion_in_methods"), "_STAT_LCOM_ALL.csv");
    }

    public void calculateClassComplexityQuartilePerAPK() throws IOException {
        resultToCSV(calculateQuartilePerApk("Class", "class_complexity"), "_STAT_CLASS_COMPLEXITY_ALL.csv");
    }

    public void calculateCyclomaticComplexityQuartilePerAPK() throws IOException {
        resultToCSV(calculateQuartilePerApk("Method", "cyclomatic_complexity"), "_STAT_CYCLO_COMPLEXITY_ALL.csv");
    }

    public void deleteQuery(String appKey) throws CypherException, IOException {
        Result result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n {app_key: '"+appKey+"'})-[r]-() DELETE n,r");
            System.out.println(result.resultAsString());
            tx.success();
        }
    }

    public void calculateClassComplexityQuartile() throws IOException {
        statsToCSV(calculateQuartile("Class", "class_complexity"), "_STAT_CLASS_COMPLEXITY.csv");
    }

    public void calculateCyclomaticComplexityQuartile() throws IOException {
        statsToCSV(calculateQuartile("Method", "cyclomatic_complexity"), "_STAT_CYCLOMATIC_COMPLEXITY.csv");
    }

    public void calculateNumberofInstructionsQuartile() throws IOException {
        statsToCSV(calculateQuartile("Method", "number_of_instructions"),"_STAT_NB_INSTRUCTIONS.csv");
    }



    /**
     * Excluding classes implementing 0 or 1 interface
     * @return
     */
    public void calculateNumberOfImplementedInterfacesQuartile() throws IOException {
        Map<String, Double> res = new HashMap<>();
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE n.number_of_implemented_interfaces > 1 RETURN percentileDisc(n.number_of_implemented_interfaces,0.25) as Q1, percentileDisc(n.number_of_implemented_interfaces,0.5) as MED, percentileDisc(n.number_of_implemented_interfaces,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            //Only one result in that case
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                double q1 = (int) row.get("Q1");
                double med = (int) row.get("MED");
                double q3 = (int) row.get("Q3");
                double threshold  = q3 + ( 1.5 * ( q3 - q1));
                res.put("Q1",q1);
                res.put("Q3",q3);
                res.put("MED",med);
                res.put("THRESHOLD",threshold);
            }
        }
        statsToCSV(res,"_STAT_NB_INTERFACES.csv");
    }

    public void calculateNumberOfMethodsForInterfacesQuartile() throws IOException {
        Map<String, Double> res = new HashMap<>();
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE HAS(n.is_interface) RETURN percentileDisc(n.number_of_methods,0.25) as Q1, percentileDisc(n.number_of_methods,0.5) as MED, percentileDisc(n.number_of_methods,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            //Only one result in that case
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                double q1 = (int) row.get("Q1");
                double med = (int) row.get("MED");
                double q3 = (int) row.get("Q3");
                double threshold  = q3 + ( 1.5 * ( q3 - q1));
                res.put("Q1",q1);
                res.put("Q3",q3);
                res.put("MED",med);
                res.put("THRESHOLD",threshold);
            }
        }
        statsToCSV(res,"_STAT_NB_METHODS_INTERFACE.csv");
    }

    public void calculateLackofCohesionInMethodsQuartile() throws IOException {
        statsToCSV(calculateQuartile("Class", "lack_of_cohesion_in_methods"),"_STAT_LCOM.csv");
    }

    public void calculateNumberOfMethodsQuartile() throws IOException {
        statsToCSV(calculateQuartile("Class", "number_of_methods"),"_STAT_NB_METHODS.csv");
    }

    public void calculateNumberOfAttributesQuartile() throws IOException {
        statsToCSV(calculateQuartile("Class", "number_of_attributes"),"_STAT_NB_ATTRIBUTES.csv");
    }

    private void resultToCSV(Result result,String csvSuffix) throws IOException {
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

    private void statsToCSV(Map<String,Double> stats, String csvSuffix) throws IOException {
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
            result = graphDatabaseService.execute("MATCH (n:Variable) return n.app_key,count(n)");
            resultToCSV(result,"_COUNT_VARIABLE.csv");
        }
    }

    public void countInnerClasses() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Class) WHERE has(n.is_inner_class) return n.app_key,count(n)");
            resultToCSV(result,"_COUNT_INNER.csv");
        }
    }

    public void countAsyncClasses() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Class{parent_name:'android.os.AsyncTask'}) return n.app_key,count(n) as number_of_async");
            resultToCSV(result,"_COUNT_ASYNC.csv");
        }
    }

    public void countViews() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (n:Class{parent_name:'android.view.View'}) return n.app_key,count(n) as number_of_views");
            resultToCSV(result,"_COUNT_VIEWS.csv");
        }
    }
}
