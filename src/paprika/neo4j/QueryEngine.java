package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.util.List;

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

}
