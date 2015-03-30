package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class QueryEngine {

    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private ExecutionEngine engine;

    public QueryEngine(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        engine = new ExecutionEngine(graphDatabaseService);
    }

    public void shutDown(){
        databaseManager.shutDown();
    }

   public void MIMQuery() throws CypherException {
       ExecutionResult result;
       try (Transaction ignored = graphDatabaseService.beginTx()) {
           result = engine.execute("MATCH (m1:Method)\n" +
                   "WHERE NOT HAS(m1.`is_static`)\n" +
                   "AND NOT m1-[:USES]->(:Variable)\n" +
                   "AND NOT (m1)-[:CALLS]->(:Method)\n" +
                   "RETURN m1.app_key,count(m1)\n" +
                   "ORDER BY m1.app_key");
           List<String> columns = result.columns();
           System.out.println(result.dumpToString());
       }
   }


    public double calculateHighThreshold(String nodeType, String property){
        double res = 0;
        ExecutionResult result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:"+nodeType+") RETURN percentileDisc(n."+property+",0.25) as Q1, percentileDisc(n."+property+",0.75) as Q3";
            result = engine.execute(query);
            //Only one result in that case
            for ( Map<String, Object> row : result )
            {
                int q1 = (int) row.get("Q1");
                int q3 = (int) row.get("Q3");
                res = q3 + ( 1.5 * ( q3 - q1));
            }
        }
        return res;
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

}
