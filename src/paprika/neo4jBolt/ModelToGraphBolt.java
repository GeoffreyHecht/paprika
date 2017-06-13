
package paprika.neo4jBolt;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

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
public class ModelToGraphBolt {
	private Graph graph;
	private Session session;

	private static final String appLabel = "App";
	private static final String classLabel = "Class";
	private static final String externalClassLabel = "ExternalClass";
	private static final String methodLabel = "Method";
	private static final String externalMethodLabel = "ExternalMethod";
	private static final String variableLabel = "Variable";
	private static final String argumentLabel = "Argument";
	private static final String externalArgumentLabel = "ExternalArgument";

	private Map<Entity, LowNode> methodNodeMap;
	private Map<PaprikaClass, LowNode> classNodeMap;
	private Map<PaprikaVariable, LowNode> variableNodeMap;

	private long key;

	public ModelToGraphBolt() {

		graph = new Graph();
		session = DriverBolt.getSession();
		methodNodeMap = new HashMap<>();
		classNodeMap = new HashMap<>();
		variableNodeMap = new HashMap<>();

	}

	public LowNode insertApp(PaprikaApp paprikaApp, LowNode nodeVer) {
		this.key = nodeVer.getID();
		LowNode appNode = new LowNode(appLabel);
		StatementResult result;
		
		try (Transaction tx = this.session.beginTransaction()) {
		
			appNode.addParameter("app_key", key);
			appNode.addParameter("name", paprikaApp.getName());
			appNode.addParameter("version", paprikaApp.getVersionName());
			Date date = new Date();
			SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
			appNode.addParameter("date_analysis", simpleFormat.format(date));

			result = tx.run(graph.create(appNode));
			long id = this.graph.getID(result, Graph.NAMELABEL);
			appNode.setId(id);
			LowNode nodeVerSet = new LowNode(nodeVer.getLabel());
			long idi = nodeVer.getID();
			if (idi == -1)
				nodeVerSet.setId(idi);
			
			
			for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
				tx.run(graph.relation(appNode, insertClass(paprikaClass, tx), RelationTypes.APP_OWNS_CLASS.name()));
			}
			for (PaprikaExternalClass paprikaExternalClass : paprikaApp.getPaprikaExternalClasses()) {
				insertExternalClass(paprikaExternalClass,tx);
			}
			nodeVerSet.addParameter(Graph.ANALYSEINLOAD, "80");
			tx.run(graph.set(nodeVer, nodeVerSet));

			
			for (Metric metric : paprikaApp.getMetrics()) {
				insertMetric(metric, appNode);
			}

			nodeVerSet.addParameter(Graph.ANALYSEINLOAD, "90");
			tx.run(graph.set(nodeVer, nodeVerSet));

			LowNode rawNode = new LowNode(Graph.LABELAPP);
			rawNode.setId(id);
			tx.run(graph.set(rawNode, appNode));
			nodeVerSet.addParameter(Graph.ANALYSEINLOAD, "95");

			
			tx.success();
		}
		try (Transaction tx = this.session.beginTransaction()) {
			createHierarchy(paprikaApp, tx);
			createCallGraph(paprikaApp, tx);
			tx.success();
		}
		return appNode;
	}

	private void insertMetric(Metric metric, LowNode node) {
		node.addParameter(metric.getName(), metric.getValue());
	}

	public LowNode insertClass(PaprikaClass paprikaClass, Transaction tx) {
		LowNode classNode = new LowNode(classLabel);
		classNodeMap.put(paprikaClass, classNode);
		classNode.addParameter("app_key", key);
		classNode.addParameter("name", paprikaClass.getName());
		classNode.addParameter("modifier", paprikaClass.getModifier().toString().toLowerCase());
		if (paprikaClass.getParentName() != null) {
			classNode.addParameter("parent_name", paprikaClass.getParentName());
		}

		StatementResult result = tx.run(graph.create(classNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		classNode.setId(id);

		
		for (PaprikaVariable paprikaVariable : paprikaClass.getPaprikaVariables()) {
			tx.run(graph.relation(classNode, insertVariable(paprikaVariable, tx),
					RelationTypes.CLASS_OWNS_VARIABLE.name()));
		}
		for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
			tx.run(graph.relation(classNode, insertMethod(paprikaMethod, tx), RelationTypes.CLASS_OWNS_METHOD.name()));
		}
		for (Metric metric : paprikaClass.getMetrics()) {
			insertMetric(metric, classNode);
		}
		
		LowNode rawNode = new LowNode(classLabel);
		rawNode.setId(id);
		tx.run(graph.set(rawNode, classNode));

		return classNode;
	}

	
	public LowNode insertExternalClass(PaprikaExternalClass paprikaClass, Transaction tx) {
		LowNode classNode = new LowNode(externalClassLabel);
		classNode.addParameter("app_key", key);
		classNode.addParameter("name", paprikaClass.getName());
		if (paprikaClass.getParentName() != null) {
			classNode.addParameter("parent_name", paprikaClass.getParentName());
		}
		
		StatementResult result = tx.run(graph.create(classNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		classNode.setId(id);
		
		for (PaprikaExternalMethod paprikaExternalMethod : paprikaClass.getPaprikaExternalMethods()) {
			tx.run(graph.relation(classNode, insertExternalMethod(paprikaExternalMethod, tx),
					RelationTypes.CLASS_OWNS_METHOD.name()));
		}
		for (Metric metric : paprikaClass.getMetrics()) {
			insertMetric(metric, classNode);
		}
		LowNode rawNode = new LowNode(externalClassLabel);
		rawNode.setId(id);
		tx.run(graph.set(rawNode, classNode));

		
		return classNode;
	}

	public LowNode insertVariable(PaprikaVariable paprikaVariable, Transaction tx) {
		LowNode variableNode = new LowNode(variableLabel);
		variableNodeMap.put(paprikaVariable, variableNode);
		variableNode.addParameter("app_key", key);
		variableNode.addParameter("name", paprikaVariable.getName());
		variableNode.addParameter("modifier", paprikaVariable.getModifier().toString().toLowerCase());
		variableNode.addParameter("type", paprikaVariable.getType());

		StatementResult result = tx.run(graph.create(variableNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		variableNode.setId(id);
		
		for (Metric metric : paprikaVariable.getMetrics()) {
			insertMetric(metric, variableNode);
		}
		LowNode rawNode = new LowNode(variableLabel);
		rawNode.setId(id);
		tx.run(graph.set(rawNode, variableNode));
		return variableNode;
	}

	public LowNode insertMethod(PaprikaMethod paprikaMethod, Transaction tx) {
		LowNode methodNode = new LowNode(methodLabel);
		methodNodeMap.put(paprikaMethod, methodNode);
		methodNode.addParameter("app_key", key);
		methodNode.addParameter("name", paprikaMethod.getName());
		methodNode.addParameter("modifier", paprikaMethod.getModifier().toString().toLowerCase());
		methodNode.addParameter("full_name", paprikaMethod.toString());
		methodNode.addParameter("return_type", paprikaMethod.getReturnType());
		for (Metric metric : paprikaMethod.getMetrics()) {
			insertMetric(metric, methodNode);
		}
		
		StatementResult result = tx.run(graph.create(methodNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		methodNode.setId(id);

		
		LowNode variableNode;
		for (PaprikaVariable paprikaVariable : paprikaMethod.getUsedVariables()) {
			variableNode = variableNodeMap.get(paprikaVariable);
			if (variableNode != null) {
				tx.run(graph.relation(methodNode, variableNodeMap.get(paprikaVariable), RelationTypes.USES.name()));
			} else {
				System.out.println("problem");
			}

		}
		for (PaprikaArgument arg : paprikaMethod.getArguments()) {
			tx.run(graph.relation(methodNode, insertArgument(arg, tx), RelationTypes.METHOD_OWNS_ARGUMENT.name()));
		}
		return methodNode;
	}

	public LowNode insertExternalMethod(PaprikaExternalMethod paprikaMethod, Transaction tx) {
		LowNode methodNode = new LowNode(externalMethodLabel);
		methodNodeMap.put(paprikaMethod, methodNode);
		methodNode.addParameter("app_key", key);
		methodNode.addParameter("name", paprikaMethod.getName());
		methodNode.addParameter("full_name", paprikaMethod.toString());
		methodNode.addParameter("return_type", paprikaMethod.getReturnType());
		for (Metric metric : paprikaMethod.getMetrics()) {
			insertMetric(metric, methodNode);
		}

		StatementResult result = tx.run(graph.create(methodNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		methodNode.setId(id);
		
		for (PaprikaExternalArgument arg : paprikaMethod.getPaprikaExternalArguments()) {
			tx.run(graph.relation(methodNode, insertExternalArgument(arg, tx),
					RelationTypes.METHOD_OWNS_ARGUMENT.name()));
		}
		return methodNode;
	}

	public LowNode insertArgument(PaprikaArgument paprikaArgument, Transaction tx) {
		LowNode argNode = new LowNode(argumentLabel);
		argNode.addParameter("app_key", key);
		argNode.addParameter("name", paprikaArgument.getName());
		argNode.addParameter("position", paprikaArgument.getPosition());
		
		StatementResult result = tx.run(graph.create(argNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		argNode.setId(id);

		return argNode;
	}

	public LowNode insertExternalArgument(PaprikaExternalArgument paprikaExternalArgument, Transaction tx) {
		LowNode argNode = new LowNode(externalArgumentLabel);
		argNode.addParameter("app_key", key);
		argNode.addParameter("name", paprikaExternalArgument.getName());
		argNode.addParameter("position", paprikaExternalArgument.getPosition());
		for (Metric metric : paprikaExternalArgument.getMetrics()) {
			insertMetric(metric, argNode);
		}

		StatementResult result = tx.run(graph.create(argNode));
		long id = this.graph.getID(result, Graph.NAMELABEL);
		argNode.setId(id);

		return argNode;
	}

	public void createHierarchy(PaprikaApp paprikaApp, Transaction tx) {
		for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
			PaprikaClass parent = paprikaClass.getParent();
			if (parent != null) {
				tx.run(graph.relation(classNodeMap.get(paprikaClass), classNodeMap.get(parent),
						RelationTypes.EXTENDS.name()));

			}
			for (PaprikaClass pInterface : paprikaClass.getInterfaces()) {
				tx.run(graph.relation(classNodeMap.get(paprikaClass), classNodeMap.get(pInterface),
						RelationTypes.IMPLEMENTS.name()));
			}
		}
	}

	public void createCallGraph(PaprikaApp paprikaApp, Transaction tx) {
		for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()) {
			for (PaprikaMethod paprikaMethod : paprikaClass.getPaprikaMethods()) {
				for (Entity calledMethod : paprikaMethod.getCalledMethods()) {
					tx.run(graph.relation(methodNodeMap.get(paprikaMethod), methodNodeMap.get(calledMethod),
							RelationTypes.CALLS.name()));
				}
			}
		}
	}
}
