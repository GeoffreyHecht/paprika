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
public class IGSQuery extends PaprikaQuery {

    public static final String KEY = "IGS";

    public IGSQuery() {
        super(KEY);
    }

    /*
     MATCH (a:App) WITH a.app_key as key
     MATCH (cl:Class {app_key: key})-[:CLASS_OWNS_METHOD]->(m1:Method {app_key: key})-[:CALLS]->(m2:Method {app_key: key})
     WHERE (m2.is_setter OR m2.is_getter) AND (cl)-[:CLASS_OWNS_METHOD]->(m2)
     RETURN m1.app_key as app_key

     details -> m1.full_name as full_name
                m2.full_name as gs_name
     else -> count(m1) as IGS
     */

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (a:App) WITH a.app_key as key\n" +
                "MATCH (cl:Class {app_key: key})-[:CLASS_OWNS_METHOD]->(m1:Method {app_key: key})-[:CALLS]->(m2:Method {app_key: key})\n" +
                "WHERE (m2.is_setter OR m2.is_getter) AND (cl)-[:CLASS_OWNS_METHOD]->(m2)\n" +
                "RETURN m1.app_key as app_key,";
        if (details) {
            query += "m1.full_name as full_name, m2.full_name as gs_name";
        } else {
            query += "count(m1) as IGS";
        }
        return query;
    }

}
