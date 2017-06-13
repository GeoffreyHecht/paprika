package paprika.neo4jBolt;

import java.net.InetAddress;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.driver.v1.types.Node;

/**
 * 
 * @author guillaume willefert Graph is a class who contains many method for
 *         create a correct command String
 *         But also many String key.
 *         And a unique driver for all graph.
 *         
 *         
 *         
 *         This is a small Blob.
 * 
 */
public class Graph {

	public static final String LABELAPP = "Code";
	public static final String LABELQUERY = "CodeSmells";

	public static final String REL_CAS_CODE="HAS_CODESMELL";
	public static final String REL_VERSION_CODE = "IS_STRUCTURED";
	public static final String REL_VERSION_CODESMELLS = "EXHIBITS";

	public static final String REL_CODESMELLS_CAS="RESULT";
	
	public static final String APPKEY = "app_key";
	public static final String CODEA = "code_is_analyzed";
	public static final String NAMELABEL = "target";
	public static final String VERSIONLABEL = "Version";
	public static final String ANALYSEINLOAD = "analyseInLoading";
	
	
	
	
	
	
	private static final String CREATEIT = "  CREATE (it)-[:";
	private static final String RETURN = " RETURN ";

	/**
	 * Create a node with the label and with this parameter
	 * 
	 * @param lowNode
	 * @return
	 */
	public String create(LowNode lowNode) {
		/* créer une donnée */
		return "CREATE (" + Graph.NAMELABEL + ":" + lowNode.getLabel() + lowNode.parametertoData() + ")"
				+ Graph.RETURN + Graph.NAMELABEL;
	}

	/**
	 * Récupère l'id du node du premier record
	 * 
	 * @param result
	 * @return
	 */
	public long getID(StatementResult result, String labelNode) {
		if (result!=null && result.hasNext() && labelNode!=null) {
			Record record = result.next();
			Node node = record.get(labelNode).asNode();
			if (node != null) {
				return node.id();
			}
		}
		return -1;

	}

	/**
	 * Return a command who Create a relation between two nodes who exist.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String relation(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		return matchPrefabs("it", lowNode) + matchPrefabs(Graph.NAMELABEL, lowNodeTarget) + Graph.CREATEIT
				+ relationLabel + "]->(" + Graph.NAMELABEL + ")";
	}

	/**
	 * Return a command who Create a relation between two nodes who exist.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String relation(LowNode lowNode, LowNode lowNodeTarget, LowNode lowNodeRelation) {
		return matchPrefabs("it", lowNode) + matchPrefabs(Graph.NAMELABEL, lowNodeTarget) + Graph.CREATEIT
				+ lowNodeRelation.getLabel() + lowNodeRelation.parametertoData() + "]->(" + Graph.NAMELABEL
				+ ")";
	}

	/**
	 * Return a command who Create a relation between two nodes, where the left
	 * node already exist
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String relationcreateRight(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		return matchPrefabs("it", lowNode) + Graph.CREATEIT + relationLabel + "]->(" + Graph.NAMELABEL + ":"
				+ lowNodeTarget.getLabel() + lowNodeTarget.parametertoData() + ")";
	}

	/**
	 * Utilisez pour toutes les fonctions, si le premier paramètre est labelID
	 * alors il n'y a pas d'autres paramètres à part l'id et on renvoie un match
	 * where id. Sinon, on applique un match avec l'ensemble des paramètres.
	 * 
	 * @param labelname
	 * @param lowNode
	 * @return
	 */

	public String matchPrefabs(String labelname, LowNode lowNode) {
		return " MATCH (" + labelname + ":" + lowNode.getLabel() + lowNode.parametertoData() + ") "
				+ lowNode.idfocus(labelname);
	}

	/**
	 * Return a command for clean all database
	 *
	 * private String deleteAll() { return "MATCH (n) DETACH DELETE n"; }
	 */

	/**
	 * renvoie une commande qui retourne les nodes d'un label avec pour
	 * conditions, parameter.
	 * 
	 * @param lowNode
	 * @return
	 */

	public String matchSee(LowNode lowNode) {
		return matchPrefabs(Graph.NAMELABEL, lowNode) + Graph.RETURN + Graph.NAMELABEL;
	}

	/**
	 * Retourne une commande qui retourne tous les nodes du node donné en
	 * paramètre lié à leur labelRelation.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String matchSee(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		return matchPrefabs("a", lowNode) + matchPrefabs(Graph.NAMELABEL, lowNodeTarget) + " MATCH (a)-[:"
				+ relationLabel + "]->(" + Graph.NAMELABEL + ")" + Graph.RETURN + Graph.NAMELABEL;
	}

	/**
	 * parameter doit contenir toujours id, pour que set fonctionne, set
	 * applique ensuite le reste des paramètres au node.
	 * 
	 * @param lowNode
	 * @param newAttributsNode
	 * @return
	 */
	public String set(LowNode lowNode, LowNode newAttributsNode) {
		String result = this.matchPrefabs(Graph.NAMELABEL, lowNode);
		StringBuilder str = newAttributsNode.parametertoData();
		if (str.length() != 0) {
			result += " SET " + Graph.NAMELABEL + "+=" + str;
		}
		result += Graph.RETURN + Graph.NAMELABEL;
		return result;
	}

	/**
	 * Supprime une donnée de la base de donnée et tous ces enfants et petits
	 * enfants.
	 * 
	 * @param lowNode
	 * @return
	 */
	public String deleteDataAndAllChildrends(LowNode lowNode) {
		return this.matchPrefabs("n", lowNode) + " MATCH (n)-[*]->(a)" + " DETACH DELETE n,a";
	}

}
