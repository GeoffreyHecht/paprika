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

package paprika.neo4jBolt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class QuartileCalculator {
	protected QueryEngineBolt queryEngine;
	protected Session session;

	public QuartileCalculator(QueryEngineBolt queryEngine) {
		this.queryEngine = queryEngine;
		session = DriverBolt.getSession();
	}

	public void calculateClassComplexityQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Class  {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE NOT EXISTS(n.is_interface)  AND NOT EXISTS(n.is_abstract) RETURN n as nod,percentileCont(n.class_complexity,0.25) as Q1, percentileCont(n.class_complexity,0.5) as MED, percentileCont(n.class_complexity,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_CLASS_COMPLEXITY");
	}

	public void calculateCyclomaticComplexityQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Method  {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE NOT EXISTS(n.is_getter)  AND NOT EXISTS(n.is_setter) AND n.cyclomatic_complexity > 0 RETURN n as nod,percentileCont(n.cyclomatic_complexity,0.25) as Q1, percentileCont(n.cyclomatic_complexity,0.5) as MED, percentileCont(n.cyclomatic_complexity,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_CYCLOMATIC_COMPLEXITY");
	}

	public void calculateNumberofInstructionsQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Method  {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE NOT EXISTS(n.is_getter)  AND NOT EXISTS(n.is_setter) AND n.number_of_instructions > 0 RETURN n as nod,percentileCont(n.number_of_instructions,0.25) as Q1, percentileCont(n.number_of_instructions,0.5) as MED, percentileCont(n.number_of_instructions,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_NB_INSTRUCTIONS");
	}

	public Map calculateQuartile(String nodeType, String property) {
		StatementResult result = null;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:" + nodeType + " {app_key:" + queryEngine.getKeyApp()
					+ "}) RETURN n as nod, percentileCont(n." + property + ",0.25) as Q1,percentileCont(n." + property
					+ ",0.5) as MED, percentileCont(n." + property + ",0.75) as Q3";
			result = tx.run(query);
			tx.success();
		}
		return calculeTresholds(result);

	}

	private Map calculeTresholds(StatementResult result) {
		Map<String, Double> res = new HashMap<>();
		// Only one result in that case
		while (result.hasNext()) {

			Map<String, Object> row = result.next().asMap();
			// Sometime neo4J return a double or an int... With toString it's
			// works in all cases

			// Neo4J return always a result, but it can create a "false" node
			// who have all key, but null value , when the match send nothing
			if (row.get("Q1") == null)
				continue;

			double q1 = Double.valueOf(row.get("Q1").toString());
			double med = Double.valueOf(row.get("MED").toString());
			double q3 = Double.valueOf(row.get("Q3").toString());
			double high = q3 + (1.5 * (q3 - q1));
			double very_high = q3 + (3 * (q3 - q1));
			res.put("Q1", q1);
			res.put("Q3", q3);
			res.put("MED", med);
			res.put("HIGH", high);
			res.put("VERY_HIGH", very_high);
		}
		return res;
	}

	/**
	 * Excluding classes implementing 0 or 1 interface
	 * 
	 * @return
	 */
	public void calculateNumberOfImplementedInterfacesQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Class  {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE n.number_of_implemented_interfaces > 1   RETURN n as nod, percentileCont(n.number_of_implemented_interfaces,0.25) as Q1, percentileCont(n.number_of_implemented_interfaces,0.5) as MED, percentileCont(n.number_of_implemented_interfaces,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_NB_INTERFACES");
	}

	public void calculateNumberOfMethodsForInterfacesQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Class {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE EXISTS(n.is_interface)  RETURN n as nod,percentileCont(n.number_of_methods,0.25) as Q1, percentileCont(n.number_of_methods,0.5) as MED, percentileCont(n.number_of_methods,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_NB_METHODS_INTERFACE");
	}

	public void calculateLackofCohesionInMethodsQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Class {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE NOT EXISTS(n.is_interface) AND NOT EXISTS(n.is_abstract) RETURN n as nod,percentileCont(n.lack_of_cohesion_in_methods,0.25) as Q1, percentileCont(n.lack_of_cohesion_in_methods,0.5) as MED, percentileCont(n.lack_of_cohesion_in_methods,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_LCOM");
	}

	public void calculateNumberOfMethodsQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Class {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE NOT EXISTS(n.is_interface) AND NOT EXISTS(n.is_abstract) RETURN n as nod,percentileCont(n.number_of_methods,0.25) as Q1, percentileCont(n.number_of_methods,0.5) as MED, percentileCont(n.number_of_methods,0.75) as Q3";
			result = tx.run(query);
			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_NB_METHODS");
	}

	public void calculateNumberOfAttributesQuartile() throws IOException {
		Map<String, Double> res;
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			String query = "MATCH (n:Class {app_key:" + queryEngine.getKeyApp()
					+ "}) WHERE NOT EXISTS(n.is_interface) AND NOT EXISTS(n.is_abstract) RETURN n as nod,percentileCont(n.number_of_attributes,0.25) as Q1, percentileCont(n.number_of_attributes,0.5) as MED, percentileCont(n.number_of_attributes,0.75) as Q3";
			result = tx.run(query);

			res = calculeTresholds(result);
			tx.success();
		}
		queryEngine.statsToCSV(res, "STAT_NB_ATTRIBUTES");
	}
}
