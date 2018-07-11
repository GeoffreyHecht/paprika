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
import paprika.metrics.classes.condition.IsInnerClassStatic;
import paprika.metrics.common.IsStatic;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.queries.QueryBuilderUtils.getClassResults;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class LICQuery extends PaprikaQuery {

    public static final String KEY = "LIC";

    public LICQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
        MATCH (cl:Class) WHERE exists(cl.is_inner_class)
        AND NOT exists(cl.is_static)
        RETURN cl.app_key as app_key

        details -> cl.name as full_name
        else -> count(cl) as LIC
     */

    @Override
    public String getQuery(boolean details) {
        Identifier aClass = identifier("cl");

        return match(node(aClass).label(CLASS_TYPE))
                .where(and(exists(aClass.property(IsInnerClassStatic.NAME)),
                        not(exists(aClass.property(IsStatic.NAME)))))
                .returns(getClassResults(aClass, details, "LIC"))
                .toString();

    }

}
