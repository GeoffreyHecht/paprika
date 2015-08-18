package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class OverdrawQuery extends Query {

    private OverdrawQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static OverdrawQuery createOverdrawQuery(QueryEngine queryEngine) {
        return new OverdrawQuery(queryEngine);
    }

    @Override
    public void execute() throws CypherException, IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (:Class{parent_name:\"android.view.View\"})-[:CLASS_OWNS_METHOD]->(n:Method{name:\"onDraw\"})-[:METHOD_OWNS_ARGUMENT]->(:Argument{position:1,name:\"android.graphics.Canvas\"}) \n" +
                    "WHERE NOT n-[:CALLS]->(:ExternalMethod{full_name:\"clipRect#android.graphics.Canvas\"}) AND NOT n-[:CALLS]->(:ExternalMethod{full_name:\"quickReject#android.graphics.Canvas\"})\n" +
                    "RETURN n.app_key as app_key,count(n) as UIO";
            Result result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_UIO.csv");
        }
    }
}
