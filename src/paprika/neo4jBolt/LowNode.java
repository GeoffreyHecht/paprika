package paprika.neo4jBolt;
import java.util.HashMap;
import java.util.Iterator;



/**
 * Celle classe contient une Arraymap et un label et l'id.
 * 
 * L'id par défaut est -1, la method idfocus étant utilisé dans la fonction qui
 * crée les matchs, permet de cibler un node, si non -1
 * 
 * LowNode ne contient pas de relation ou autre, il est juste un petit node
 * contenant le minimum d'informations. D'ailleurs, il ne peut avoir qu'un seul
 * label
 * 

 * @author guillaume
 *
 */
public class LowNode {
	private String label;
	private HashMap<String, String> map;
	private long id;

	public LowNode(String label) {
		this.label = label;
		this.map = new HashMap<>();
		this.id = -1;
	}

	public String getLabel() {
		return this.label;
	}
	public long getID(){
		return this.id;
	}

	public void addParameter(String attributeName, String value) {
		this.map.put(attributeName, overString(value));
	}
	public void addParameter(String attributeName, Object value) {
		this.map.put(attributeName, value.toString());
	}
	public void addParameter(String attributeName, int value) {
		this.map.put(attributeName, Integer.toString(value));
	}
	public void addParameter(String attributeName, long value) {
		this.map.put(attributeName, Long.toString(value));

	}
	public void addParameter(String attributeName, double value) {
		this.map.put(attributeName, Double.toString(value));
	}
	public String getParameter(String attributeName){
		return this.map.get(attributeName);
	}
	
	private String overString(String string) {
		return "\"" + string + "\"";
	}

	/**
	 * Retourne le where d'une commande cypher de neo4J qui contient la
	 * comparaison id .
	 * 
	 * @param labelname
	 * @return
	 */
	public String idfocus(String labelname) {
		if (this.id == -1)
			return "";
		return "WHERE ID(" + labelname + ") = " + this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Equivalent à un tostring d'unemap, mais au lieu de =, on a des :, ex:
	 * "{name : guillaume }"
	 * 
	 * Attention, l'ordre n'est pas ordonné, même si cela n'a pas d'importance.
	 */
	public StringBuilder parametertoData() {
		if (this.map.size() == 0)
			return new StringBuilder("");
		String begin;
		String end;
		begin = " {";
		end = "} ";
		String key;
		String value;
		Boolean onetime = false;
		StringBuilder data = new StringBuilder(begin);
		Iterator<String> iter = this.map.keySet().iterator();
		while (iter.hasNext()) {
			key = iter.next();
			value = this.map.get(key);
			if (onetime)
				data.append(" , ");
			else
				onetime = true;

			data.append(key + ":" + value);
		}
		data.append(end);

		return data;
	}
}