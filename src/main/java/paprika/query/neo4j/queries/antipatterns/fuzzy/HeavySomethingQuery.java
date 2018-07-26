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

public abstract class HeavySomethingQuery extends FuzzyQuery {

    public HeavySomethingQuery(String queryName, QueryPropertiesReader reader) {
        super(queryName, "HeavySomething.fcl", reader);
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(Result result, FIS fis) {
        int noi, cc;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        while (result.hasNext()) {
            HashMap<String, Object> res = new HashMap<>(result.next());
            cc = (int) res.get("cyclomatic_complexity");
            noi = (int) res.get("number_of_instructions");
            if (cc >= reader.get("Heavy_class_veryHigh_cc")
                    && noi >= reader.get("Heavy_class_veryHigh_noi")) {
                res.put("fuzzy_value", 1);
            } else {
                fb.setVariable("cyclomatic_complexity", cc);
                fb.setVariable("number_of_instructions", noi);
                fb.evaluate();
                res.put("fuzzy_value", fb.getVariable("res").getValue());
            }
            fuzzyResult.add(res);
        }
        return fuzzyResult;
    }

}
