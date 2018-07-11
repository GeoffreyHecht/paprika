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
import paprika.metrics.classes.stat.paprika.ClassComplexity;
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
public class CCQuery extends FuzzyQuery {

    public static final String KEY = "CC";

    protected static double high = 28;
    protected static double veryHigh = 43;

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
        Identifier aClass = identifier("cl");

        return getCCNodes(aClass, veryHigh)
                .returns(getClassResults(aClass, details, KEY))
                .toString();
    }

    /*
        MATCH (cl:Class) WHERE cl.class_complexity > high
        RETURN cl.app_key as app_key, cl.class_complexity as class_complexity

        details -> cl.name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        Identifier aClass = identifier("cl");

        List<Expression> results = new ArrayList<>();
        results.add(as(aClass.property(PaprikaClass.APP_KEY), "app_key"));
        results.add(as(aClass.property(ClassComplexity.NAME), "class_complexity"));
        if (details) {
            results.add(as(aClass.property(PaprikaClass.NAME), "full_name"));
        }

        return getCCNodes(aClass, high)
                .returns(results)
                .toString();
    }

    private Where getCCNodes(Identifier aClass, double threshold) {
        return match(node(aClass).label(CLASS_TYPE))
                .where(aClass.property(ClassComplexity.NAME).gt(threshold));
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(Result result, FIS fis) {
        int cc;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        while (result.hasNext()) {
            HashMap<String, Object> res = new HashMap<>(result.next());
            cc = (int) res.get("class_complexity");
            if (cc >= veryHigh) {
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
