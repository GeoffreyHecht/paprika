package paprika.neo4j;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;
import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.entities.PaprikaVariable;
import paprika.metrics.Metric;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private Map<PaprikaMethod,Node> methodNodeMap;
    private Map<PaprikaClass,Node> classNodeMap;

    private String key;

    public ModelToGraph(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        engine = new ExecutionEngine(graphDatabaseService);
        methodNodeMap = new HashMap<>();
        classNodeMap = new HashMap<>();
    }

   /* public void createIndex(){
        IndexDefinition indexDefinition;
        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            Schema schema = graphDatabaseService.schema();
            indexDefinition = schema.indexFor(classLabel)
                    .on( "name" )
                    .create();
            indexDefinition = schema.indexFor(methodLabel)
                    .on( "fullName" )
                    .create();
            schema.awaitIndexesOnline(10, TimeUnit.MINUTES);
            tx.success();
        }
    } */

    public void insertApp(PaprikaApp paprikaApp){
        this.key = paprikaApp.getKey();
        try ( Transaction tx = graphDatabaseService.beginTx() ){
            Node appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty("app_key",key);
            appNode.setProperty("name",paprikaApp.getName());
            appNode.setProperty("category",paprikaApp.getCategory());
            appNode.setProperty("package",paprikaApp.getPack());
            appNode.setProperty("developer",paprikaApp.getDeveloper());
            appNode.setProperty("rating",paprikaApp.getRating());
            appNode.setProperty("nb_download",paprikaApp.getNbDownload());
            appNode.setProperty("date_download",paprikaApp.getDate());
            Date date = new Date();
            SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
            appNode.setProperty("date_analysis", simpleFormat.format(date));
            appNode.setProperty("size",paprikaApp.getSize());
            appNode.setProperty("price",paprikaApp.getPrice());
            for(PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
                insertClass(paprikaClass,appNode);
            }
            for(Metric metric : paprikaApp.getMetrics()){
                insertMetric(metric, appNode);
            }
            tx.success();
        }
        //createIndex();
        try ( Transaction tx = graphDatabaseService.beginTx() ){
            createHierarchy(paprikaApp);
            createCallGraph(paprikaApp);
            tx.success();
        }
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(),metric.getValue());
    }


    public void insertClass(PaprikaClass paprikaClass, Node appNode){
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNodeMap.put(paprikaClass,classNode);
        classNode.setProperty("app_key",key);
        classNode.setProperty("name",paprikaClass.getName());
        classNode.setProperty("modifier", paprikaClass.getModifier().toString().toLowerCase());
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
        variableNode.setProperty("app_key", key);
        variableNode.setProperty("name", paprikaVariable.getName());
        variableNode.setProperty("modifier", paprikaVariable.getModifier().toString().toLowerCase());
        variableNode.setProperty("type", paprikaVariable.getType());
        classNode.createRelationshipTo(variableNode,RelationTypes.CLASS_OWNS_VARIABLE);
        for(Metric metric : paprikaVariable.getMetrics()){
            insertMetric(metric, variableNode);
        }
    }
    public void insertMethod(PaprikaMethod paprikaMethod, Node classNode ){
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNodeMap.put(paprikaMethod,methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name",paprikaMethod.getName());
        methodNode.setProperty("modifier", paprikaMethod.getModifier().toString().toLowerCase());
        methodNode.setProperty("full_name",paprikaMethod.toString());
        methodNode.setProperty("return_type",paprikaMethod.getReturnType());
        classNode.createRelationshipTo(methodNode,RelationTypes.CLASS_OWNS_METHOD);
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
    }

    public void createHierarchy(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            if (parent != null) {
                //Cypher Query to crate the relationship
                /*Map<String, Object> params = new HashMap<>();
                params.put( "className", paprikaClass.getName());
                params.put( "parentName", parent.getName());
                String query = "MATCH (c:Class),(p:Class) WHERE c.name={className} AND p.name={parentName} CREATE UNIQUE (c)-[r:"+RelationTypes.EXTENDS+"]->(p)  RETURN r LIMIT 1";
                engine.execute( query, params );*/
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(parent),RelationTypes.EXTENDS);
            }
        }
    }

    public void createCallGraph(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
                for(PaprikaMethod calledMethod : paprikaMethod.getCalledMethods()){
                    //Cypher Query to create the relationship
                    /*Map<String, Object> params = new HashMap<>();
                    params.put( "methodName", paprikaMethod.toString());
                    params.put( "calledName", calledMethod.toString());
                    String query = "MATCH (m:Method),(c:Method) WHERE m.full_name ={methodName} AND c.full_name ={calledName} CREATE UNIQUE (m)-[r:"+RelationTypes.CALLS+" ]->(c)  RETURN r LIMIT 1";
                    engine.execute( query, params );*/
                    methodNodeMap.get(calledMethod).createRelationshipTo(methodNodeMap.get(paprikaMethod),RelationTypes.CALLS);
                }
            }
        }
    }
}
