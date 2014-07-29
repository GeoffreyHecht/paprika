package paprika.neo4j;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.entities.PaprikaVariable;
import paprika.metrics.Metric;

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
    private static final Label variableLabel = DynamicLabel.label("Variable");

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
        for(PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()){
            insertVariable(paprikaVariable, classNode);
        }
        for(PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
            insertMethod(paprikaMethod,classNode);
        }

        for(Metric metric : paprikaClass.getMetrics()){
            insertMetric(metric,classNode);
        }
    }

    public void insertVariable(PaprikaVariable paprikaVariable, Node classNode){
        Node variableNode = graphDatabaseService.createNode(variableLabel);
        variableNode.setProperty("name", paprikaVariable.getName());
        variableNode.setProperty("modifier", paprikaVariable.getModifier().toString());
        variableNode.setProperty("type", paprikaVariable.getType());
        classNode.createRelationshipTo(variableNode,RelationTypes.CLASS_OWNS_VARIABLE);
        for(Metric metric : paprikaVariable.getMetrics()){
            insertMetric(metric, variableNode);
        }
    }
    public void insertMethod(PaprikaMethod paprikaMethod, Node classNode ){
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNode.setProperty("name",paprikaMethod.getName());
        //methodNode.setProperty("public",paprikaMethod.getIsPublic());
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
                //Cypher Query to crate the relationship
                engine.execute("MATCH (c:Class),(p:Class) WHERE c.name ='" + paprikaClass.getName() +
                        "' AND p.name ='" + parent.getName() +
                        "'  CREATE (c)-[r:"+RelationTypes.EXTENDS+" ]->(p)  RETURN r");
            }
        }
    }
}
