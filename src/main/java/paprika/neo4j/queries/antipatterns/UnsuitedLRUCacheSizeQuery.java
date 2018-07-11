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
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.queries.QueryBuilderUtils.getMethodResults;
import static paprika.neo4j.queries.QueryBuilderUtils.methodCallsExternal;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class UnsuitedLRUCacheSizeQuery extends PaprikaQuery {

    public static final String KEY = "UCS";

    public UnsuitedLRUCacheSizeQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
        MATCH (m:Method)-[:CALLS]->(e:ExternalMethod {full_name:'<init>#android.util.LruCache'})
        WHERE NOT (m)-[:CALLS]->(:ExternalMethod {full_name:'getMemoryClass#android.app.ActivityManager'})
        RETURN m.app_key as app_key

        details -> m.full_name as full_name
        else -> count(m) as UCS
     */

    @Override
    public String getQuery(boolean details) {
        Identifier method = identifier("m");

        return match(methodCallsExternal(method, "<init>#android.util.LruCache"))
                .where(not(methodCallsExternal(method, "getMemoryClass#android.app.ActivityManager")))
                .returns(getMethodResults(method, details, KEY))
                .toString();
    }

}
