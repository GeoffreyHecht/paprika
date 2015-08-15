package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class CCQuery {
    protected QueryEngine queryEngine;
    protected GraphDatabaseService graphDatabaseService;
    protected static double classComplexity = 27;

    public CCQuery(QueryEngine queryEngine) {
        this.queryEngine = queryEngine;
        graphDatabaseService = queryEngine.getGraphDatabaseService();
    }

    public void execute() throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            result = graphDatabaseService.execute("MATCH (cl:Class) WHERE cl.class_complexity > "+ classComplexity +" RETURN cl.app_key as app_key,count(cl) as CC");
            queryEngine.resultToCSV(result,"_CC.csv");
        }
    }
}
