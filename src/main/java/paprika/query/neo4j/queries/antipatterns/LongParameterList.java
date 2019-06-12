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

import paprika.analyse.metrics.methods.stat.NumberOfParameters;
import paprika.query.neo4j.queries.PaprikaQuery;

public class LongParameterList extends PaprikaQuery {

    public static final String KEY = "LPL";

    private static final int THRESHOLD = 5;

    public LongParameterList() {
        super(KEY);
    }

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (m:Method) \n" +
                "WHERE m." + NumberOfParameters.NAME + " > " + THRESHOLD + "\n" +
                "RETURN m.app_key AS app_key,";
        if (details) {
            query += "m.full_name AS full_name," +
                    " m." + NumberOfParameters.NAME + " AS " + NumberOfParameters.NAME;
        } else {
            query += "count(m) AS LPL";
        }
        return query;
    }
}
