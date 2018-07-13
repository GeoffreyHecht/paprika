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
import org.neo4j.graphdb.Result;
import paprika.neo4j.QueryEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static paprika.neo4j.queries.QueryPropertiesReader.PROPERTIES;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class CCQuery extends FuzzyQuery {

    public static final String KEY = "CC";

    public CCQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine, "ComplexClass.fcl");
    }

    /*
        MATCH (cl:Class)
        WHERE cl.class_complexity > veryHigh
        RETURN cl.app_key as app_key

        details -> cl.name as full_name
        else -> count(cl) as CC
     */

    @Override
    public String getQuery(boolean details) {
        String query = getCCNodes(PROPERTIES.get("Class_complexity_veryHigh"));
        query += "RETURN cl.app_key as app_key,";
        if (details) {
            query += "cl.name as full_name";
        } else {
            query += "count(cl) as CC";
        }
        return query;
    }

    /*
        MATCH (cl:Class) WHERE cl.class_complexity > high
        RETURN cl.app_key as app_key, cl.class_complexity as class_complexity

        details -> cl.name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        String query = getCCNodes(PROPERTIES.get("Class_complexity_high"));
        query += "RETURN cl.app_key as app_key, cl.class_complexity as class_complexity";
        if (details) {
            query += ",cl.name as full_name";
        }
        return query;
    }

    private String getCCNodes(double threshold) {
        return "MATCH (cl:Class)\n" +
                "WHERE cl.class_complexity > " + threshold + "\n";
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(Result result, FIS fis) {
        int cc;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        while (result.hasNext()) {
            HashMap<String, Object> res = new HashMap<>(result.next());
            cc = (int) res.get("class_complexity");
            if (cc >= PROPERTIES.get("Class_complexity_veryHigh")) {
                res.put("fuzzy_value", 1);
            } else {
                fb.setVariable("class_complexity", cc);
                fb.evaluate();
                res.put("fuzzy_value", fb.getVariable("res").getValue());
            }
            fuzzyResult.add(res);
        }
        return fuzzyResult;
    }


}
