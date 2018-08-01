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

package paprika.query.neo4j.queries.antipatterns.adoctor;

import paprika.analyse.metrics.classes.condition.IsCloseable;
import paprika.query.neo4j.queries.PaprikaQuery;

public class UnclosedCloseable extends PaprikaQuery {

    public static final String KEY = "UC";

    public UnclosedCloseable() {
        super(KEY);
    }

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (cl:Class)-[:CLASS_OWNS_METHOD]->(m:Method {name:'close'}) \n" +
                "WHERE cl." + IsCloseable.NAME + "\n" +
                "AND NOT (m)-[:CALLS]->(:ExternalMethod {name:'close'}) \n" +
                "AND NOT (m)-[:CALLS]->(:Method {name:'close'}) \n " +
                "RETURN cl.app_key AS app_key,";
        if (details) {
            query += "cl.name AS full_name";
        } else {
            query += "count(cl) AS UC";
        }
        return query;
    }

}
