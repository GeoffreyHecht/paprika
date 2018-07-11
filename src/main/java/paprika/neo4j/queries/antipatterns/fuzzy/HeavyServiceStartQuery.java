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

import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.grammar.Where;
import paprika.entities.PaprikaMethod;
import paprika.metrics.classes.condition.subclass.IsService;
import paprika.metrics.methods.stat.CyclomaticComplexity;
import paprika.metrics.methods.stat.NumberOfInstructions;
import paprika.neo4j.QueryEngine;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.ModelToGraph.METHOD_TYPE;
import static paprika.neo4j.RelationTypes.CLASS_OWNS_METHOD;
import static paprika.neo4j.queries.QueryBuilderUtils.getMethodResults;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class HeavyServiceStartQuery extends HeavySomethingQuery {

    public static final String KEY = "HSS";

    public HeavyServiceStartQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
        MATCH (c:Class{is_service:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'})
        WHERE m.number_of_instructions > veryHigh_noi
            AND m.cyclomatic_complexity > veryHigh_cc
        RETURN m.app_key as app_key

        details -> m.full_name as full_name
        else -> count(m) as HSS
     */

    @Override
    public String getQuery(boolean details) {
        Identifier aClass = identifier("c");
        Identifier method = identifier("m");

        return getHSSNodes(aClass, method, veryHigh_noi, veryHigh_cc)
                .returns(getMethodResults(method, details, KEY))
                .toString();
    }

    /*
        MATCH (c:Class{is_service:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'})
        WHERE m.number_of_instructions > high_noi
            AND m.cyclomatic_complexity > high_cc
        RETURN m.app_key as app_key,m.cyclomatic_complexity as cyclomatic_complexity,
            m.number_of_instructions as number_of_instructions

        details -> m.full_name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        Identifier aClass = identifier("c");
        Identifier method = identifier("m");

        return getHSSNodes(aClass, method, high_noi, high_cc)
                .returns(super.getFuzzyQueryResults(method, details))
                .toString();
    }

    private Where getHSSNodes(Identifier aClass, Identifier method, double noiThreshold, double ccThreshold) {
        return match(node(aClass).label(CLASS_TYPE).values(value(IsService.NAME, true))
                .out(CLASS_OWNS_METHOD)
                .node(method).label(METHOD_TYPE).values(value(PaprikaMethod.NAME, "onStartCommand")))
                .where(and(
                        method.property(NumberOfInstructions.NAME).gt(noiThreshold),
                        method.property(CyclomaticComplexity.NAME).gt(ccThreshold)));
    }

}
