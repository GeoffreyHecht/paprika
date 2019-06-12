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

import paprika.query.neo4j.queries.PaprikaQuery;

public class DurableWakelock extends PaprikaQuery {

    public static final String KEY = "DW";

    public DurableWakelock() {
        super(KEY);
    }

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (m:Method)-[:CALLS]->(e:ExternalMethod {full_name:'acquire#android.os.PowerManager$WakeLock'}) " +
                "WHERE NOT (e)-[:METHOD_OWNS_ARGUMENT]->(:ExternalArgument) " +
                "AND NOT (m)-[:CALLS]->(:ExternalMethod {full_name:'release#android.os.PowerManager$WakeLock'}) " +
                "RETURN m.app_key AS app_key,";
        if (details) {
            query += "m.full_name AS full_name";
        } else {
            query += "count(m) AS DW";
        }
        return query;
    }

}
