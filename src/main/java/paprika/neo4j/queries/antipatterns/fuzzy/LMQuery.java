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

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class LMQuery extends FuzzyQuery {

    public static final String KEY = "LM";
    protected static double high = 17;
    protected static double veryHigh = 26;

    public LMQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine, "LongMethod.fcl");
    }

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (m:Method) WHERE m.number_of_instructions >" + veryHigh + " RETURN m.app_key as app_key";
        if (details) {
            query += ",m.full_name as full_name";
        } else {
            query += ",count(m) as LM";
        }
        return query;
    }

    @Override
    public String getFuzzyQuery(boolean details) {
        String query = "MATCH (m:Method) WHERE m.number_of_instructions >" + high +
                " RETURN m.app_key as app_key,m.number_of_instructions as number_of_instructions";
        if (details) {
            query += ",m.full_name as full_name";
        }
        return query;
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(Result result, FIS fis) {
        int cc;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        while (result.hasNext()) {
            HashMap<String, Object> res = new HashMap<>(result.next());
            cc = (int) res.get("number_of_instructions");
            if (cc >= veryHigh) {
                res.put("fuzzy_value", 1);
            } else {
                fb.setVariable("number_of_instructions", cc);
                fb.evaluate();
                res.put("fuzzy_value", fb.getVariable("res").getValue());
            }
            fuzzyResult.add(res);
        }
        return fuzzyResult;
    }
}
