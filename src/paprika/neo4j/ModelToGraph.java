package paprika.neo4j;

import org.neo4j.graphdb.*;
import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.metrics.Metric;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public class ModelToGraph {
    private GraphDatabaseService graphDatabaseService;
    private DatabaseManager databaseManager;

    private static final Label appLabel = DynamicLabel.label("App");
    private static final Label classLabel = DynamicLabel.label("Class");
    private static final Label methodLabel = DynamicLabel.label("Method");
    private static final Label metricLabel = DynamicLabel.label("Metric");

    public ModelToGraph(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
    }

    public void insertApp(PaprikaApp paprikaApp){
        try ( Transaction tx = graphDatabaseService.beginTx() ){
            Node appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty("Name",paprikaApp.getName());
            for(PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
                insertClass(paprikaClass,appNode);
            }
            for(Metric metric : paprikaApp.getMetrics()){
                insertMetric(metric,appNode);
            }
            tx.success();
        }
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(),metric.getValue());
    }


    public void insertClass(PaprikaClass paprikaClass, Node appNode){
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNode.setProperty("Name",paprikaClass.getName());
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
        methodNode.setProperty("Name",paprikaMethod.getName());
        classNode.createRelationshipTo(methodNode,RelationTypes.CLASS_OWNS_METHOD);
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
    }
}
