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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import paprika.entities.*;
import paprika.metrics.Metric;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public class ModelToGraph {

    public static final String APP_TYPE = "App";
    public static final String CLASS_TYPE = "Class";
    public static final String EXTERNAL_CLASS_TYPE = "ExternalClass";
    public static final String METHOD_TYPE = "Method";
    public static final String EXTERNAL_METHOD_TYPE = "ExternalMethod";
    public static final String VARIABLE_TYPE = "Variable";
    public static final String ARGUMENT_TYPE = "Argument";
    public static final String EXTERNAL_ARGUMENT_TYPE = "ExternalArgument";

    private GraphDatabaseService graphDatabaseService;
    private static final Label appLabel = Label.label(APP_TYPE);
    private static final Label classLabel = Label.label(CLASS_TYPE);
    private static final Label externalClassLabel = Label.label(EXTERNAL_CLASS_TYPE);
    private static final Label methodLabel = Label.label(METHOD_TYPE);
    private static final Label externalMethodLabel = Label.label(EXTERNAL_METHOD_TYPE);
    private static final Label variableLabel = Label.label(VARIABLE_TYPE);
    private static final Label argumentLabel = Label.label(ARGUMENT_TYPE);
    private static final Label externalArgumentLabel = Label.label(EXTERNAL_ARGUMENT_TYPE);

    private Map<Entity, Node> methodNodeMap;
    private Map<PaprikaClass, Node> classNodeMap;
    private Map<PaprikaVariable, Node> variableNodeMap;

    private String key;

    public ModelToGraph(String DatabasePath) {
        DatabaseManager databaseManager = new DatabaseManager(DatabasePath);
        databaseManager.start();
        this.graphDatabaseService = databaseManager.getGraphDatabaseService();
        methodNodeMap = new HashMap<>();
        classNodeMap = new HashMap<>();
        variableNodeMap = new HashMap<>();
        IndexManager indexManager = new IndexManager(graphDatabaseService);
        indexManager.createIndex();
    }

    public void insertApp(PaprikaApp paprikaApp) {
        this.key = paprikaApp.getKey();
        Node appNode;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            appNode = graphDatabaseService.createNode(appLabel);
            appNode.setProperty(PaprikaApp.APP_KEY, key);
            appNode.setProperty(PaprikaApp.NAME, paprikaApp.getName());
            appNode.setProperty(PaprikaApp.CATEGORY, paprikaApp.getCategory());
            appNode.setProperty(PaprikaApp.PACKAGE, paprikaApp.getPack());
            appNode.setProperty(PaprikaApp.DEVELOPER, paprikaApp.getDeveloper());
            appNode.setProperty(PaprikaApp.RATING, paprikaApp.getRating());
            appNode.setProperty(PaprikaApp.NB_DOWN, paprikaApp.getNbDownload());
            appNode.setProperty(PaprikaApp.DATE_DOWN, paprikaApp.getDate());
            appNode.setProperty(PaprikaApp.VERSION_CODE, paprikaApp.getVersionCode());
            appNode.setProperty(PaprikaApp.VERSION_NAME, paprikaApp.getVersionName());
            appNode.setProperty(PaprikaApp.SDK, paprikaApp.getSdkVersion());
            appNode.setProperty(PaprikaApp.TARGET_SDK, paprikaApp.getTargetSdkVersion());
            Date date = new Date();
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
            appNode.setProperty(PaprikaApp.DATE_ANALYSIS, simpleFormat.format(date));
            appNode.setProperty(PaprikaApp.SIZE, paprikaApp.getSize());
            appNode.setProperty(PaprikaApp.PRICE, paprikaApp.getPrice());
            for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
                appNode.createRelationshipTo(insertClass(paprikaClass), RelationTypes.APP_OWNS_CLASS);
            }
            for (PaprikaExternalClass paprikaExternalClass : paprikaApp.getPaprikaExternalClasses()) {
                insertExternalClass(paprikaExternalClass);
            }
            for (Metric metric : paprikaApp.getMetrics()) {
                insertMetric(metric, appNode);
            }
            tx.success();
        }
        try (Transaction tx = graphDatabaseService.beginTx()) {
            createHierarchy(paprikaApp);
            createCallGraph(paprikaApp);
            tx.success();
        }
    }

    private void insertMetric(Metric metric, Node node) {
        node.setProperty(metric.getName(), metric.getValue());
    }


    public Node insertClass(PaprikaClass paprikaClass) {
        Node classNode = graphDatabaseService.createNode(classLabel);
        classNodeMap.put(paprikaClass, classNode);
        classNode.setProperty(PaprikaClass.APP_KEY, key);
        classNode.setProperty(PaprikaClass.NAME, paprikaClass.getName());
        classNode.setProperty(PaprikaClass.MODIFIER, paprikaClass.getModifier().toString().toLowerCase());
        if (paprikaClass.getParentName() != null) {
            classNode.setProperty(PaprikaClass.PARENT, paprikaClass.getParentName());
        }
        for (PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()) {
            classNode.createRelationshipTo(insertVariable(paprikaVariable), RelationTypes.CLASS_OWNS_VARIABLE);

        }
        for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
            classNode.createRelationshipTo(insertMethod(paprikaMethod), RelationTypes.CLASS_OWNS_METHOD);
        }
        for (Metric metric : paprikaClass.getMetrics()) {
            insertMetric(metric, classNode);
        }
        return classNode;
    }

    public void insertExternalClass(PaprikaExternalClass paprikaClass) {
        Node classNode = graphDatabaseService.createNode(externalClassLabel);
        classNode.setProperty(PaprikaExternalClass.APP_KEY, key);
        classNode.setProperty(PaprikaExternalClass.NAME, paprikaClass.getName());
        if (paprikaClass.getParentName() != null) {
            classNode.setProperty(PaprikaExternalClass.PARENT, paprikaClass.getParentName());
        }
        for (PaprikaExternalMethod paprikaExternalMethod : paprikaClass.getPaprikaExternalMethods()) {
            classNode.createRelationshipTo(insertExternalMethod(paprikaExternalMethod), RelationTypes.CLASS_OWNS_METHOD);
        }
        for (Metric metric : paprikaClass.getMetrics()) {
            insertMetric(metric, classNode);
        }
    }

    public Node insertVariable(PaprikaVariable paprikaVariable) {
        Node variableNode = graphDatabaseService.createNode(variableLabel);
        variableNodeMap.put(paprikaVariable, variableNode);
        variableNode.setProperty(PaprikaVariable.APP_KEY, key);
        variableNode.setProperty(PaprikaVariable.NAME, paprikaVariable.getName());
        variableNode.setProperty(PaprikaVariable.MODIFIER,
                paprikaVariable.getModifier().toString().toLowerCase());
        variableNode.setProperty(PaprikaVariable.TYPE, paprikaVariable.getType());
        for (Metric metric : paprikaVariable.getMetrics()) {
            insertMetric(metric, variableNode);
        }
        return variableNode;
    }

    public Node insertMethod(PaprikaMethod paprikaMethod) {
        Node methodNode = graphDatabaseService.createNode(methodLabel);
        methodNodeMap.put(paprikaMethod, methodNode);
        methodNode.setProperty(PaprikaMethod.APP_KEY, key);
        methodNode.setProperty(PaprikaMethod.NAME, paprikaMethod.getName());
        methodNode.setProperty(PaprikaMethod.MODIFIER, paprikaMethod.getModifier().toString().toLowerCase());
        methodNode.setProperty(PaprikaMethod.FULL_NAME, paprikaMethod.toString());
        methodNode.setProperty(PaprikaMethod.RETURN_TYPE, paprikaMethod.getReturnType());
        for (Metric metric : paprikaMethod.getMetrics()) {
            insertMetric(metric, methodNode);
        }
        for (PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()) {
            methodNode.createRelationshipTo(variableNodeMap.get(paprikaVariable), RelationTypes.USES);
        }
        for (PaprikaArgument arg : paprikaMethod.getArguments()) {
            methodNode.createRelationshipTo(insertArgument(arg), RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertExternalMethod(PaprikaExternalMethod paprikaMethod) {
        Node methodNode = graphDatabaseService.createNode(externalMethodLabel);
        methodNodeMap.put(paprikaMethod, methodNode);
        methodNode.setProperty(PaprikaExternalMethod.APP_KEY, key);
        methodNode.setProperty(PaprikaExternalMethod.NAME, paprikaMethod.getName());
        methodNode.setProperty(PaprikaExternalMethod.FULL_NAME, paprikaMethod.toString());
        methodNode.setProperty(PaprikaExternalMethod.RETURN_TYPE, paprikaMethod.getReturnType());
        for (Metric metric : paprikaMethod.getMetrics()) {
            insertMetric(metric, methodNode);
        }
        for (PaprikaExternalArgument arg : paprikaMethod.getPaprikaExternalArguments()) {
            methodNode.createRelationshipTo(insertExternalArgument(arg), RelationTypes.METHOD_OWNS_ARGUMENT);
        }
        return methodNode;
    }

    public Node insertArgument(PaprikaArgument paprikaArgument) {
        Node argNode = graphDatabaseService.createNode(argumentLabel);
        argNode.setProperty(PaprikaArgument.APP_KEY, key);
        argNode.setProperty(PaprikaArgument.NAME, paprikaArgument.getName());
        argNode.setProperty(PaprikaArgument.POSITION, paprikaArgument.getPosition());
        return argNode;
    }

    public Node insertExternalArgument(PaprikaExternalArgument paprikaExternalArgument) {
        Node argNode = graphDatabaseService.createNode(externalArgumentLabel);
        argNode.setProperty(PaprikaExternalArgument.APP_KEY, key);
        argNode.setProperty(PaprikaExternalArgument.NAME, paprikaExternalArgument.getName());
        argNode.setProperty(PaprikaExternalArgument.POSITION, paprikaExternalArgument.getPosition());
        for (Metric metric : paprikaExternalArgument.getMetrics()) {
            insertMetric(metric, argNode);
        }
        return argNode;
    }

    public void createHierarchy(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            PaprikaClass parent = paprikaClass.getParent();
            if (parent != null) {
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(parent), RelationTypes.EXTENDS);
            }
            for (PaprikaClass pInterface : paprikaClass.getInterfaces()) {
                classNodeMap.get(paprikaClass).createRelationshipTo(classNodeMap.get(pInterface), RelationTypes.IMPLEMENTS);
            }
        }
    }

    public void createCallGraph(PaprikaApp paprikaApp) {
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
            for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
                for (Entity calledMethod : paprikaMethod.getCalledMethods()) {
                    methodNodeMap.get(paprikaMethod).createRelationshipTo(methodNodeMap.get(calledMethod), RelationTypes.CALLS);
                }
            }
        }
    }
}
