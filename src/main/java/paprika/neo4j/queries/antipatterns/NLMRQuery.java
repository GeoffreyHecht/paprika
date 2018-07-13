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
import paprika.metrics.classes.condition.subclass.IsActivity;
import paprika.metrics.classes.condition.subclass.IsApplication;
import paprika.metrics.classes.condition.subclass.IsContentProvider;
import paprika.metrics.classes.condition.subclass.IsService;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.ModelToGraph.METHOD_TYPE;
import static paprika.neo4j.RelationTypes.CLASS_OWNS_METHOD;
import static paprika.neo4j.RelationTypes.EXTENDS;
import static paprika.neo4j.queries.QueryBuilderUtils.getClassResults;
import static paprika.neo4j.queries.QueryBuilderUtils.methodHasName;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class NLMRQuery extends PaprikaQuery {

    public static final String KEY = "NLMR";

    public NLMRQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
        OUTDATED ORIGINAL QUERY

        MATCH (cl:Class)
        WHERE exists(cl.is_activity)
            AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method { name: 'onLowMemory' })
            AND NOT (cl)-[:EXTENDS]->(:Class)
        RETURN cl.app_key as app_key

        details -> cl.name as full_name
        else -> count(cl) as NLMR
     */

    @Override
    public String getQuery(boolean details) {
        Identifier aClass = identifier("cl");
        Identifier method = identifier("m");

        return match(node(aClass).label(CLASS_TYPE))
                .where(and(
                        or(
                                exists(aClass.property(IsActivity.NAME)),
                                exists(aClass.property(IsApplication.NAME)),
                                exists(aClass.property(IsService.NAME)),
                                exists(aClass.property(IsContentProvider.NAME))),
                        not(node(aClass).label(CLASS_TYPE)
                                .out(CLASS_OWNS_METHOD)
                                .node(method).label(METHOD_TYPE)),
                        or(
                                methodHasName(method, "onLowMemory"),
                                methodHasName(method, "onTrimMemory")),
                        not(node(aClass).out(EXTENDS).node().label(CLASS_TYPE))))
                .returns(getClassResults(aClass, details, KEY))
                .toString();
    }

}
