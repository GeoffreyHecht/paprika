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
import org.neo4j.cypherdsl.expression.Expression;
import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.metrics.methods.condition.IsGetterOrSetter;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.*;
import static paprika.neo4j.RelationTypes.CALLS;
import static paprika.neo4j.RelationTypes.CLASS_OWNS_METHOD;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class IGSQuery extends PaprikaQuery {

    public static final String KEY = "IGS";

    public IGSQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
     * MATCH (a:App) WITH a.app_key as key
     * MATCH (cl:Class {app_key: key})-[:CLASS_OWNS_METHOD]->(m1:Method {app_key: key})-[:CALLS]->(m2:Method {app_key: key})
     * WHERE (m2.is_setter OR m2.is_getter) AND (cl)-[:CLASS_OWNS_METHOD]->(m2)
     * RETURN m1.app_key as app_key
     *
     * details -> m1.full_name as full_name
     *            m2.full_name as gs_name
     *
     * else -> count(m1) as "IGS
     */

    @Override
    public String getQuery(boolean details) {
        Identifier app = identifier("a");
        Identifier aClass = identifier("cl");
        Identifier method = identifier("m1");
        Identifier otherMethod = identifier("m2");
        Identifier key = identifier("key");

        List<Expression> results = new ArrayList<>();
        results.add(as(method.property(PaprikaMethod.APP_KEY), "app_key"));
        if (details) {
            results.add(as(method.property(PaprikaMethod.FULL_NAME), "full_name"));
            results.add(as(method.property(PaprikaMethod.FULL_NAME), "gs_name"));
        } else {
            results.add(as(count(method), KEY));
        }

        return match(node(app).label(APP_TYPE))
                .with(as(app.property(PaprikaApp.APP_KEY), key))
                .match(node(aClass).label(CLASS_TYPE).values(value(PaprikaClass.APP_KEY, key))
                        .out(CLASS_OWNS_METHOD)
                        .node(method).label(METHOD_TYPE).values(value(PaprikaMethod.APP_KEY, key))
                        .out(CALLS)
                        .node(otherMethod).label(METHOD_TYPE).values(value(PaprikaMethod.APP_KEY, key)))
                .where(and(
                        or(otherMethod.property(IsGetterOrSetter.IS_SETTER_NAME),
                                otherMethod.property(IsGetterOrSetter.IS_GETTER_NAME)),
                        node(aClass).out(CLASS_OWNS_METHOD).node(otherMethod)))
                .returns(results)
                .toString();

    }

}
