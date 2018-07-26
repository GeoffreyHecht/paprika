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

package paprika.query.neo4j.queries.antipatterns;

import paprika.query.neo4j.queries.PaprikaQuery;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class NLMRQuery extends PaprikaQuery {

    public static final String KEY = "NLMR";

    public NLMRQuery() {
        super(KEY);
    }

    /*
        MATCH (cl:Class)
        WHERE ( exists(cl.is_activity) OR exists(cl.is_application) OR exists (cl.is_service)
            OR exists(cl.is_content_provider))
            AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method {name = 'onLowMemory'})
            AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method {name = 'onTrimMemory'})
            AND NOT (cl)-[:EXTENDS]->(:Class)
        RETURN cl.app_key as app_key

        details -> cl.name as full_name
        else -> count(cl) as NLMR
     */

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (cl:Class)\n" +
                "WHERE ( exists(cl.is_activity) OR exists(cl.is_application) OR exists (cl.is_service)\n" +
                "       OR exists(cl.is_content_provider))\n" +
                "   AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method {name: 'onLowMemory'})\n" +
                "   AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method {name: 'onTrimMemory'})\n" +
                "   AND NOT (cl)-[:EXTENDS]->(:Class)\n" +
                "RETURN cl.app_key as app_key,";
        if (details) {
            query += "cl.name as full_name";
        } else {
            query += "count(cl) as NLMR";
        }
        return query;
    }

}
