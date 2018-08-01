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

public class RigidAlarmManager extends PaprikaQuery {

    public static final String KEY = "RAM";

    public RigidAlarmManager() {
        super(KEY);
    }

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (a:App)-[:APP_OWNS_CLASS]->(:Class)-[:CLASS_OWNS_METHOD]->" +
                "(m:Method)-[:CALLS]->(:ExternalMethod {full_name:'setRepeating#android.app.AlarmManager'}) " +
                "WHERE a.target_sdk < 19 " +
                "RETURN a.app_key AS app_key,";
        if (details) {
            query += "m.full_name AS full_name";
        } else {
            query += "count(m) AS RAM";
        }
        return query;
    }

}
