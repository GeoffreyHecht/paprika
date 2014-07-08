package paprika.neo4j;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.IteratorUtil;
import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.metrics.Metric;

import java.util.Iterator;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public class ModelToGraph {
    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;
    private ExecutionEngine engine;
    private static final Label appLabel = DynamicLabel.label("App");
    private static final Label classLabel = DynamicLabel.label("Class");
    private static final Label methodLabel = DynamicLabel.label("Method");

    public ModelToGraph(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        engine = new ExecutionEngine(graphDatabaseService);
    }

    public void insertApp(PaprikaApp paprikaApp){
        try ( Transaction tx = graphDatabaseService.beginTx() ){
            Node appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty("name",paprikaApp.getName());
            for(PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
                insertClass(paprikaClass,appNode);
            }
            for(Metric metric : paprikaApp.getMetrics()){
                insertMetric(metric,appNode);
            }
            createHierarchy(paprikaApp);
            tx.success();
        }
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(),metric.getValue());
    }


    public void insertClass(PaprikaClass paprikaClass, Node appNode){
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNode.setProperty("name",paprikaClass.getName());
        appNode.createRelationshipTo(classNode,RelationTypes.APP_OWNS_CLASS);
        for(PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
            insertMethod(paprikaMethod,classNode);
        }
        for(Metric metric : paprikaClass.getMetrics()){
            insertMetric(metric,classNode);
        }
    }

    public void insertMethod(PaprikaMethod paprikaMethod, Node classNode ){
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNode.setProperty("name",paprikaMethod.getName());
        classNode.createRelationshipTo(methodNode,RelationTypes.CLASS_OWNS_METHOD);
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
    }

    public void createHierarchy(PaprikaApp paprikaApp) {
        ExecutionResult result;
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            if (parent != null) {
                result = engine.execute("MATCH (n:`Class`) WHERE n.name ='" + paprikaClass.getName() + "' return n");
                Iterator<Node> n_column = result.columnAs("n");
                Node currentNode = null;
                for (Node node : IteratorUtil.asIterable(n_column)) {
                    currentNode = node;
                }
                result = engine.execute("MATCH (n:`Class`) WHERE n.name ='" + parent.getName() + "' return n");
                n_column = result.columnAs("n");
                for (Node node : IteratorUtil.asIterable(n_column)) {
                    currentNode.createRelationshipTo(node, RelationTypes.EXTENDS);
                }
            }
        }
    }
}
