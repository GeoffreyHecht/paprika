package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class HashMapUsageQuery extends Query {

    private HashMapUsageQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static HashMapUsageQuery createHashMapUsageQuery(QueryEngine queryEngine) {
        return new HashMapUsageQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (m:Method)-[:CALLS]->(e:ExternalMethod{full_name:'<init>#java.util.HashMap'}) return m.app_key";
            if(details){
                query += ",m.full_name as full_name";
            }else{
                query += ", count(m) as HMU";
            }
            result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_HMU.csv");
        }
    }

}
