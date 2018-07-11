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

package paprika.neo4j.queries.antipatterns;

import org.neo4j.cypherdsl.Identifier;
import paprika.metrics.common.IsStatic;
import paprika.metrics.methods.NumberOfCallers;
import paprika.metrics.methods.condition.IsInit;
import paprika.metrics.methods.condition.IsOverride;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.*;
import static paprika.neo4j.RelationTypes.CALLS;
import static paprika.neo4j.RelationTypes.USES;
import static paprika.neo4j.queries.QueryBuilderUtils.getMethodResults;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class MIMQuery extends PaprikaQuery {

    public static final String KEY = "MIM";

    public MIMQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
        MATCH (m1:Method)
        WHERE m1.number_of_callers > 0 AND NOT exists(m1.is_static)
            AND NOT exists(m1.is_override)
            AND NOT (m1)-[:USES]->(:Variable)
            AND NOT (m1)-[:CALLS]->(:ExternalMethod)
            AND NOT (m1)-[:CALLS]->(:Method)
            AND NOT exists(m1.is_init)
        RETURN m1.app_key as app_key

        details -> m1.full_name as full_name
        else -> count(m1) as MIM
     */

    @Override
    public String getQuery(boolean details) {
        Identifier method = identifier("m1");

        return match(node(method).label(METHOD_TYPE))
                .where(and(
                        method.property(NumberOfCallers.NAME).gt(0),
                        not(exists(method.property(IsStatic.NAME))),
                        not(exists(method.property(IsOverride.NAME))),
                        not(node(method).out(USES).node().label(VARIABLE_TYPE)),
                        not(node(method).out(CALLS).node().label(EXTERNAL_METHOD_TYPE)),
                        not(node(method).out(CALLS).node().label(METHOD_TYPE)),
                        not(exists(method.property(IsInit.NAME)))))
                .returns(getMethodResults(method, details, KEY))
                .toString();
    }

}
