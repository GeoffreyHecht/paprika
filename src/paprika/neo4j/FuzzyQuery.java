package paprika.neo4j;

import org.neo4j.cypher.CypherException;

import java.io.IOException;

/**
 * Created by Geoffrey Hecht on 17/08/15.
 */
public abstract class FuzzyQuery extends Query {
    protected String fclFile;

    public FuzzyQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public abstract void executeFuzzy() throws CypherException, IOException;
}
