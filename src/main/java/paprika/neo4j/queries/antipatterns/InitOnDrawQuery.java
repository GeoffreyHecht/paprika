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
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.ModelToGraph.METHOD_TYPE;
import static paprika.neo4j.RelationTypes.CALLS;
import static paprika.neo4j.RelationTypes.CLASS_OWNS_METHOD;
import static paprika.neo4j.queries.QueryBuilderUtils.ANDROID_VIEW;
import static paprika.neo4j.queries.QueryBuilderUtils.getMethodResults;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class InitOnDrawQuery extends PaprikaQuery {

    public static final String KEY = "IOD";

    public InitOnDrawQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
     * MATCH (:Class{parent_name:'android.view.View'})-[:CLASS_OWNS_METHOD]->
     *  (n:Method{name:'onDraw'})-[:CALLS]->({name:'<init>'})
     * RETURN n.app_key as app_key
     *
     * details -> n.full_name as full_name
     *
     * else -> count(n) as IOD
     */

    @Override
    public String getQuery(boolean details) {
        Identifier method = identifier("n");

        return match(node().label(CLASS_TYPE).values(value(PaprikaClass.PARENT, ANDROID_VIEW))
                .out(CLASS_OWNS_METHOD)
                .node(method).label(METHOD_TYPE).values(value(PaprikaMethod.NAME, "onDraw"))
                .out(CALLS)
                .node().values(value("name", "<init>")))
                .returns(getMethodResults(method, details, KEY))
                .toString();
    }

}
