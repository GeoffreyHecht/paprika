package paprika.neo4j;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class IndexManager {
    private GraphDatabaseService graphDatabaseService;

    public IndexManager(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    public void createIndex(){
        IndexDefinition indexDefinition;
        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            Schema schema = graphDatabaseService.schema();
            org.neo4j.graphdb.index.IndexManager index = graphDatabaseService.index();
            if(!schema.getIndexes().iterator().hasNext()) {
                schema.indexFor(DynamicLabel.label("Variable"))
                        .on("app_key")
                        .create();
                schema.indexFor(DynamicLabel.label("Method"))
                        .on("app_key")
                        .create();
                schema.indexFor(DynamicLabel.label("Method"))
                        .on("is_static")
                        .create();
                schema.indexFor(DynamicLabel.label("Argument"))
                        .on("app_key")
                        .create();
                schema.indexFor(DynamicLabel.label("Class"))
                        .on("app_key")
                        .create();
                RelationshipIndex roles = index.forRelationships("calls");
            }
            tx.success();
        }
    }
}
