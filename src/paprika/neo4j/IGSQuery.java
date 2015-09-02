package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class IGSQuery extends Query {

    private IGSQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static IGSQuery createIGSQuery(QueryEngine queryEngine) {
        return new IGSQuery(queryEngine);
    }

    @Override
    public void execute() throws CypherException, IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (a:App) WITH a.app_key as key MATCH (cl:Class {app_key: key})-[:CLASS_OWNS_METHOD]->(m1:Method {app_key: key})-[:CALLS]->(m2:Method {app_key: key}) WHERE (m2.is_setter OR m2.is_getter) AND cl-[:CLASS_OWNS_METHOD]->m2 RETURN m1.app_key as app_key,count(m1) as IGS";
            Result result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_IGS_NO_FUZZY.csv");
        }
    }
}
