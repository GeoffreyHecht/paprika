/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprika.neo4j;

import org.neo4j.graphdb.*;
import paprika.entities.*;
import paprika.entities.Entity;
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
    private static final Label appLabel = DynamicLabel.label("App");
    private static final Label classLabel = DynamicLabel.label("Class");
    private static final Label externalClassLabel = DynamicLabel.label("ExternalClass");
    private static final Label methodLabel = DynamicLabel.label("Method");
    private static final Label externalMethodLabel = DynamicLabel.label("ExternalMethod");
    private static final Label variableLabel = DynamicLabel.label("Variable");
    private static final Label argumentLabel = DynamicLabel.label("Argument");
    private static final Label externalArgumentLabel = DynamicLabel.label("ExternalArgument");

    private Map<Entity,Node> methodNodeMap;
    private Map<PaprikaClass,Node> classNodeMap;
    private Map<PaprikaVariable,Node> variableNodeMap;

    private String key;

    public ModelToGraph(String DatabasePath){
        this.databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        methodNodeMap = new HashMap<>();
        classNodeMap = new HashMap<>();
        variableNodeMap = new HashMap<>();
        IndexManager indexManager = new IndexManager(graphDatabaseService);
        indexManager.createIndex();
    }

    public Node insertApp(PaprikaApp paprikaApp){
        this.key = paprikaApp.getKey();
        Node appNode;
        try ( Transaction tx = graphDatabaseService.beginTx() ){
            appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty("app_key",key);
            appNode.setProperty("name",paprikaApp.getName());
            appNode.setProperty("category",paprikaApp.getCategory());
            appNode.setProperty("package",paprikaApp.getPack());
            appNode.setProperty("developer",paprikaApp.getDeveloper());
            appNode.setProperty("rating",paprikaApp.getRating());
            appNode.setProperty("nb_download",paprikaApp.getNbDownload());
            appNode.setProperty("date_download",paprikaApp.getDate());
            appNode.setProperty("version_code",paprikaApp.getVersionCode());
            appNode.setProperty("version_name",paprikaApp.getVersionName());
            appNode.setProperty("sdk",paprikaApp.getSdkVersion());
            appNode.setProperty("target_sdk",paprikaApp.getTargetSdkVersion());
            Date date = new Date();
            SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
            appNode.setProperty("date_analysis", simpleFormat.format(date));
            appNode.setProperty("size",paprikaApp.getSize());
            appNode.setProperty("price",paprikaApp.getPrice());
            for(PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
                appNode.createRelationshipTo(insertClass(paprikaClass),RelationTypes.APP_OWNS_CLASS);
            }
            for(PaprikaExternalClass paprikaExternalClass : paprikaApp.getPaprikaExternalClasses()){
                insertExternalClass(paprikaExternalClass);
            }
            for(Metric metric : paprikaApp.getMetrics()){
                insertMetric(metric, appNode);
            }
            tx.success();
        }
        try ( Transaction tx = graphDatabaseService.beginTx() ){
            createHierarchy(paprikaApp);
            createCallGraph(paprikaApp);
            tx.success();
        }
        return appNode;
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(),metric.getValue());
    }


    public Node insertClass(PaprikaClass paprikaClass){
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNodeMap.put(paprikaClass,classNode);
        classNode.setProperty("app_key",key);
        classNode.setProperty("name",paprikaClass.getName());
        classNode.setProperty("modifier", paprikaClass.getModifier().toString().toLowerCase());
        if(paprikaClass.getParentName() != null){
            classNode.setProperty("parent_name", paprikaClass.getParentName());
        }
        for(PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()){
            classNode.createRelationshipTo(insertVariable(paprikaVariable),RelationTypes.CLASS_OWNS_VARIABLE);

        }
        for(PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
            classNode.createRelationshipTo(insertMethod(paprikaMethod),RelationTypes.CLASS_OWNS_METHOD);
        }
        for(Metric metric : paprikaClass.getMetrics()){
            insertMetric(metric,classNode);
        }
        return classNode;
    }

    public Node insertExternalClass(PaprikaExternalClass paprikaClass){
        Node classNode = graphDatabaseService.createNode(externalClassLabel);
        classNode.setProperty("app_key",key);
        classNode.setProperty("name",paprikaClass.getName());
        if(paprikaClass.getParentName() != null){
            classNode.setProperty("parent_name", paprikaClass.getParentName());
        }
        for(PaprikaExternalMethod paprikaExternalMethod : paprikaClass.getPaprikaExternalMethods()){
            classNode.createRelationshipTo(insertExternalMethod(paprikaExternalMethod),RelationTypes.CLASS_OWNS_METHOD);
        }
        for(Metric metric : paprikaClass.getMetrics()){
            insertMetric(metric,classNode);
        }
        return classNode;
    }

    public Node insertVariable(PaprikaVariable paprikaVariable){
        Node variableNode = graphDatabaseService.createNode(variableLabel);
        variableNodeMap.put(paprikaVariable,variableNode);
        variableNode.setProperty("app_key", key);
        variableNode.setProperty("name", paprikaVariable.getName());
        variableNode.setProperty("modifier", paprikaVariable.getModifier().toString().toLowerCase());
        variableNode.setProperty("type", paprikaVariable.getType());
        for(Metric metric : paprikaVariable.getMetrics()){
            insertMetric(metric, variableNode);
        }
        return variableNode;
    }
    
    public Node insertMethod(PaprikaMethod paprikaMethod){
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNodeMap.put(paprikaMethod,methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name",paprikaMethod.getName());
        methodNode.setProperty("modifier", paprikaMethod.getModifier().toString().toLowerCase());
        methodNode.setProperty("full_name",paprikaMethod.toString());
        methodNode.setProperty("return_type",paprikaMethod.getReturnType());
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
        for(PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()){
            methodNode.createRelationshipTo(variableNodeMap.get(paprikaVariable),RelationTypes.USES);
        }
        for(PaprikaArgument arg : paprikaMethod.getArguments()){
            methodNode.createRelationshipTo(insertArgument(arg),RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertExternalMethod(PaprikaExternalMethod paprikaMethod){
        Node methodNode = graphDatabaseService.createNode(externalMethodLabel);
        methodNodeMap.put(paprikaMethod,methodNode);
        methodNode.setProperty("app_key", key);
        methodNode.setProperty("name",paprikaMethod.getName());
        methodNode.setProperty("full_name",paprikaMethod.toString());
        methodNode.setProperty("return_type",paprikaMethod.getReturnType());
        for(Metric metric : paprikaMethod.getMetrics()){
            insertMetric(metric, methodNode);
        }
        for(PaprikaExternalArgument arg : paprikaMethod.getPaprikaExternalArguments()){
            methodNode.createRelationshipTo(insertExternalArgument(arg),RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertArgument(PaprikaArgument paprikaArgument){
        Node argNode = graphDatabaseService.createNode(argumentLabel);
        argNode.setProperty("app_key", key);
        argNode.setProperty("name", paprikaArgument.getName());
        argNode.setProperty("position", paprikaArgument.getPosition());
        return argNode;
    }

    public Node insertExternalArgument(PaprikaExternalArgument paprikaExternalArgument){
        Node argNode = graphDatabaseService.createNode(externalArgumentLabel);
        argNode.setProperty("app_key", key);
        argNode.setProperty("name", paprikaExternalArgument.getName());
        argNode.setProperty("position", paprikaExternalArgument.getPosition());
        for(Metric metric : paprikaExternalArgument.getMetrics()){
            insertMetric(metric, argNode);
        }
        return argNode;
    }

    public void createHierarchy(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            if (parent != null) {
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(parent),RelationTypes.EXTENDS);
            }
            for(PaprikaClass pInterface : paprikaClass.getInterfaces()){
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(pInterface),RelationTypes.IMPLEMENTS);
            }
        }
    }

    public void createCallGraph(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()){
                for(Entity calledMethod : paprikaMethod.getCalledMethods()){
                    methodNodeMap.get(paprikaMethod).createRelationshipTo(methodNodeMap.get(calledMethod),RelationTypes.CALLS);
                }
            }
        }
    }
}
