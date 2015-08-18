package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class NLMRQuery extends Query {

    private NLMRQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static NLMRQuery createNLMRQuery(QueryEngine queryEngine) {
        return new NLMRQuery(queryEngine);
    }

    @Override
    public void execute() throws CypherException, IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (cl:Class) WHERE HAS(cl.is_activity) AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method { name: 'onLowMemory' }) AND NOT cl-[:EXTENDS]->(:Class) RETURN cl.app_key as app_key,count(cl) as NLMR";
            Result result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_NLMR.csv");
        }
    }
}
