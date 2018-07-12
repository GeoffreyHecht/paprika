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

package paprika.neo4j.queries.stats;

import org.neo4j.cypherdsl.Identifier;
import paprika.metrics.classes.condition.IsInnerClassStatic;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.queries.QueryBuilderUtils.getCountResults;

public class CountInnerQuery extends PaprikaQuery {

    public static final String COMMAND_KEY = "COUNTINNER";

    public CountInnerQuery(QueryEngine queryEngine) {
        super("COUNT_INNER", queryEngine);
    }

    /*
        MATCH (n:Class) WHERE exists(n.is_inner_class)
        RETURN n.app_key as app_key,count(n) as nb_inner_classes
     */

    @Override
    public String getQuery(boolean details) {
        Identifier aClass = identifier("n");

        return match(node(aClass).label(CLASS_TYPE))
                .where(exists(aClass.property(IsInnerClassStatic.NAME)))
                .returns(getCountResults(aClass, "nb_inner_classes"))
                .toString();
    }

}
