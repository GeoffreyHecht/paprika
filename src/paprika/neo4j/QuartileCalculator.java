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
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class QuartileCalculator {
    protected QueryEngine queryEngine;
    protected GraphDatabaseService graphDatabaseService;

    public QuartileCalculator(QueryEngine queryEngine) {
        this.queryEngine = queryEngine;
        graphDatabaseService = queryEngine.getGraphDatabaseService();
    }


    public void calculateClassComplexityQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE NOT exists(n.is_interface) AND NOT exists(n.is_abstract) RETURN percentileCont(n.class_complexity,0.25) as Q1, percentileCont(n.class_complexity,0.5) as MED, percentileCont(n.class_complexity,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_CLASS_COMPLEXITY.csv");
    }

    public void calculateCyclomaticComplexityQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Method) WHERE NOT exists(n.is_getter) AND NOT exists(n.is_setter) AND n.cyclomatic_complexity > 0 RETURN percentileCont(n.cyclomatic_complexity,0.25) as Q1, percentileCont(n.cyclomatic_complexity,0.5) as MED, percentileCont(n.cyclomatic_complexity,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_CYCLOMATIC_COMPLEXITY.csv");
    }

    public void calculateNumberofInstructionsQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Method) WHERE NOT exists(n.is_getter) AND NOT exists(n.is_setter) AND n.number_of_instructions > 0 RETURN percentileCont(n.number_of_instructions,0.25) as Q1, percentileCont(n.number_of_instructions,0.5) as MED, percentileCont(n.number_of_instructions,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_NB_INSTRUCTIONS.csv");
    }

    public Map calculateQuartile(String nodeType, String property){
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:" + nodeType + ") RETURN percentileCont(n." + property + ",0.25) as Q1,percentileCont(n." + property + ",0.5) as MED, percentileCont(n." + property + ",0.75) as Q3";
            result = graphDatabaseService.execute(query);
            return calculeTresholds(result);
        }
    }

    private Map calculeTresholds(Result result){
        Map<String, Double> res = new HashMap<>();
        //Only one result in that case
        while (result.hasNext())
        {
            Map<String,Object> row = result.next();
            //Sometime neo4J return a double or an int... With toString it's works in all cases
            double q1 = Double.valueOf(row.get("Q1").toString());
            double med = Double.valueOf(row.get("MED").toString());
            double q3 = Double.valueOf(row.get("Q3").toString());
            double high  = q3 + ( 1.5 * ( q3 - q1));
            double very_high  = q3 + ( 3 * ( q3 - q1));
            res.put("Q1",q1);
            res.put("Q3",q3);
            res.put("MED",med);
            res.put("HIGH (1.5)",high);
            res.put("VERY HIGH (3.0)",very_high);
        }
        return res;
    }

    /**
     * Excluding classes implementing 0 or 1 interface
     * @return
     */
    public void calculateNumberOfImplementedInterfacesQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE n.number_of_implemented_interfaces > 1 RETURN percentileCont(n.number_of_implemented_interfaces,0.25) as Q1, percentileCont(n.number_of_implemented_interfaces,0.5) as MED, percentileCont(n.number_of_implemented_interfaces,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_NB_INTERFACES.csv");
    }

    public void calculateNumberOfMethodsForInterfacesQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE exists(n.is_interface) RETURN percentileCont(n.number_of_methods,0.25) as Q1, percentileCont(n.number_of_methods,0.5) as MED, percentileCont(n.number_of_methods,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_NB_METHODS_INTERFACE.csv");
    }

    public void calculateLackofCohesionInMethodsQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE NOT exists(n.is_interface) AND NOT exists(n.is_abstract) RETURN percentileCont(n.lack_of_cohesion_in_methods,0.25) as Q1, percentileCont(n.lack_of_cohesion_in_methods,0.5) as MED, percentileCont(n.lack_of_cohesion_in_methods,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_LCOM.csv");
    }

    public void calculateNumberOfMethodsQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE NOT exists(n.is_interface) AND NOT exists(n.is_abstract) RETURN percentileCont(n.number_of_methods,0.25) as Q1, percentileCont(n.number_of_methods,0.5) as MED, percentileCont(n.number_of_methods,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_NB_METHODS.csv");
    }

    public void calculateNumberOfAttributesQuartile() throws IOException {
        Map<String, Double> res;
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (n:Class) WHERE NOT exists(n.is_interface) AND NOT exists(n.is_abstract) RETURN percentileCont(n.number_of_attributes,0.25) as Q1, percentileCont(n.number_of_attributes,0.5) as MED, percentileCont(n.number_of_attributes,0.75) as Q3";
            result = graphDatabaseService.execute(query);
            res = calculeTresholds(result);
        }
        queryEngine.statsToCSV(res, "_STAT_NB_ATTRIBUTES.csv");
    }
}
