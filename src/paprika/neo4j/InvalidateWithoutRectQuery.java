package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class InvalidateWithoutRectQuery extends Query {

    private InvalidateWithoutRectQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static InvalidateWithoutRectQuery createInvalidateWithoutRectQuery(QueryEngine queryEngine) {
        return new InvalidateWithoutRectQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query ="MATCH (:Class{parent_name:'android.view.View'})-[:CLASS_OWNS_METHOD]->(n:Method{name:'onDraw'})-[:CALLS]->(e:ExternalMethod{name:'invalidate'}) WHERE NOT e-[:METHOD_OWNS_ARGUMENT]->(:ExternalArgument) return n.app_key";
            if(details){
                query += ",n.full_name as full_name";
            }else{
                query += ",count(n) as IWR";
            }
            result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_IWR.csv");
        }
    }

}
