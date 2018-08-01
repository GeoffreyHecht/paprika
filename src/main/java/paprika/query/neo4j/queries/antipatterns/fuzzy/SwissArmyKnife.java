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

package paprika.query.neo4j.queries.antipatterns.fuzzy;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import org.neo4j.graphdb.Result;
import paprika.query.neo4j.queries.QueryPropertiesReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class SwissArmyKnife extends FuzzyQuery {

    public static final String KEY = "SAK";

    public SwissArmyKnife(QueryPropertiesReader reader) {
        super(KEY, "SwissArmyKnife.fcl", reader);
    }

    /*
        MATCH (cl:Class)
        WHERE exists(cl.is_interface)
            AND cl.number_of_methods > veryHigh
        RETURN cl.app_key as app_key

        details -> cl.name as full_name
        else -> count(cl) as SAK
     */

    @Override
    public String getQuery(boolean details) {
        String query = getSAKNodes(reader.get("SAK_methods_veryHigh"));
        query += "RETURN cl.app_key as app_key,";
        if (details) {
            query +="cl.name as full_name";
        } else {
            query += " count(cl) as SAK";
        }
        return query;
    }

    /*
        MATCH (cl:Class)
        WHERE exists(cl.is_interface)
            AND cl.number_of_methods > high
        RETURN cl.app_key as app_key,cl.number_of_methods as number_of_methods

        details -> cl.name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        String query = getSAKNodes(reader.get("SAK_methods_high"));
        query += "RETURN cl.app_key as app_key,cl.number_of_methods as number_of_methods";
        if (details) {
            query +=",cl.name as full_name";
        }
        return query;
    }

    private String getSAKNodes(double threshold) {
        return "MATCH (cl:Class)\n" +
                "WHERE exists(cl.is_interface)\n" +
                "   AND cl.number_of_methods > " + threshold + "\n";
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(Result result, FIS fis) {
        int cc;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        while (result.hasNext()) {
            HashMap<String, Object> res = new HashMap<>(result.next());
            cc = (int) res.get("number_of_methods");
            if (cc >= reader.get("SAK_methods_veryHigh")) {
                res.put("fuzzy_value", 1);
            } else {
                fb.setVariable("number_of_methods", cc);
                fb.evaluate();
                res.put("fuzzy_value", fb.getVariable("res").getValue());
            }
            fuzzyResult.add(res);
        }
        return fuzzyResult;
    }
}
