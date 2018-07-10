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

package paprika.neo4j.queries.antipatterns.fuzzy;

import paprika.neo4j.QueryEngine;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class HeavyAsyncTaskStepsQuery extends HeavySomethingQuery {

    public static final String KEY = "HAS";

    public HeavyAsyncTaskStepsQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (c:Class{parent_name:'android.os.AsyncTask'})-[:CLASS_OWNS_METHOD]->(m:Method) " +
                "WHERE (m.name='onPreExecute' OR m.name='onProgressUpdate' OR m.name='onPostExecute') " +
                "AND  m.number_of_instructions >" + veryHigh_noi + " " +
                "AND m.cyclomatic_complexity > " + veryHigh_cc + " " +
                "RETURN m.app_key as app_key";
        if (details) {
            query += ",m.full_name as full_name";
        } else {
            query += ",count(m) as HAS";
        }
        return query;
    }

    @Override
    public String getFuzzyQuery(boolean details) {
        String query = "MATCH (c:Class{parent_name:'android.os.AsyncTask'})-[:CLASS_OWNS_METHOD]->(m:Method) " +
                "WHERE (m.name='onPreExecute' OR m.name='onProgressUpdate' OR m.name='onPostExecute') " +
                "AND  m.number_of_instructions >" + high_noi +
                " AND m.cyclomatic_complexity > " + high_cc +
                " RETURN m.app_key as app_key,m.cyclomatic_complexity as cyclomatic_complexity," +
                " m.number_of_instructions as number_of_instructions";
        if (details) {
            query += ",m.full_name as full_name";
        }
        return query;
    }


}
