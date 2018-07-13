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

package paprika.neo4j.queries.antipatterns.fuzzy;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.expression.Expression;
import org.neo4j.cypherdsl.grammar.Where;
import org.neo4j.graphdb.Result;
import paprika.entities.PaprikaClass;
import paprika.metrics.classes.stat.paprika.LackOfCohesionInMethods;
import paprika.metrics.classes.stat.soot.NumberOfAttributes;
import paprika.metrics.common.NumberOfMethods;
import paprika.neo4j.QueryEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.queries.QueryBuilderUtils.getClassResults;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class BLOBQuery extends FuzzyQuery {

    public static final String KEY = "BLOB";

    public static double high_lcom;
    public static double veryHigh_lcom;
    public static double high_noa;
    public static double veryHigh_noa;
    public static double high_nom;
    public static double veryHigh_nom;

    public BLOBQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine, "Blob.fcl");
    }

    /*
        MATCH (cl:Class)
        WHERE cl.lack_of_cohesion_in_methods > veryHigh_lcom
            AND cl.number_of_methods > veryHigh_nom
            AND cl.number_of_attributes > veryHigh_noa
        RETURN cl.app_key as app_key

        details -> cl.name as full_name,
        else -> count(cl) as BLOB
     */

    @Override
    public String getQuery(boolean details) {
        Identifier aClass = identifier("cl");

        return getBLOBNodes(aClass, veryHigh_lcom, veryHigh_nom, veryHigh_noa)
                .returns(getClassResults(aClass, details, "BLOB"))
                .toString();
    }

    /*
        MATCH (cl:Class)
        WHERE cl.lack_of_cohesion_in_methods > high_lcom
            AND cl.number_of_methods > high_nom
            AND cl.number_of_attributes > high_noa
        RETURN cl.app_key as app_key, cl.lack_of_cohesion_in_methods as lack_of_cohesion_in_methods,
            cl.number_of_methods as number_of_methods, cl.number_of_attributes as number_of_attributes

        details -> cl.name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        Identifier aClass = identifier("cl");

        List<Expression> results = new ArrayList<>();
        results.add(as(aClass.property(PaprikaClass.APP_KEY), "app_key"));
        results.add(as(aClass.property(LackOfCohesionInMethods.NAME), "lack_of_cohesion_in_methods"));
        results.add(as(aClass.property(NumberOfMethods.NAME), "number_of_methods"));
        results.add(as(aClass.property(NumberOfAttributes.NAME), "number_of_attributes"));
        if (details) {
            results.add(as(aClass.property(PaprikaClass.NAME), "full_name"));
        }

        return getBLOBNodes(aClass, high_lcom, high_nom, high_noa)
                .returns(results)
                .toString();
    }

    private Where getBLOBNodes(Identifier aClass, double lcomThreshold, double nomThreshold, double noaThreshold) {
        return match(node(aClass).label(CLASS_TYPE))
                .where(and(
                        aClass.property(LackOfCohesionInMethods.NAME).gt(lcomThreshold),
                        aClass.property(NumberOfMethods.NAME).gt(nomThreshold),
                        aClass.property(NumberOfAttributes.NAME).gt(noaThreshold)));
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(Result result, FIS fis) {
        int lcom, noa, nom;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        while (result.hasNext()) {
            HashMap<String, Object> res = new HashMap<>(result.next());
            lcom = (int) res.get("lack_of_cohesion_in_methods");
            noa = (int) res.get("number_of_attributes");
            nom = (int) res.get("number_of_methods");
            if (lcom >= veryHigh_lcom && noa >= veryHigh_noa && nom >= veryHigh_nom) {
                res.put("fuzzy_value", 1);
            } else {
                fb.setVariable("lack_of_cohesion_in_methods", lcom);
                fb.setVariable("number_of_attributes", noa);
                fb.setVariable("number_of_methods", nom);
                fb.evaluate();
                res.put("fuzzy_value", fb.getVariable("res").getValue());
            }
            fuzzyResult.add(res);
        }
        return fuzzyResult;
    }


}
