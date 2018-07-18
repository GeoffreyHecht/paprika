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

import static paprika.neo4j.queries.QueryPropertiesReader.PROPERTIES;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class HeavyServiceStartQuery extends HeavySomethingQuery {

    public static final String KEY = "HSS";

    public HeavyServiceStartQuery() {
        super(KEY);
    }

    /*
        MATCH (c:Class{is_service:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'})
        WHERE m.number_of_instructions > veryHigh_noi
            AND m.cyclomatic_complexity > veryHigh_cc
        RETURN m.app_key as app_key

        details -> m.full_name as full_name
        else -> count(m) as HSS
     */

    @Override
    public String getQuery(boolean details) {
        String query = getHSSNodes(PROPERTIES.get("Heavy_class_veryHigh_noi"),
                PROPERTIES.get("Heavy_class_veryHigh_cc"));
        query += " RETURN m.app_key as app_key,";
        if (details) {
            query += "m.full_name as full_name";
        } else {
            query += " count(m) as HSS";
        }
        return query;
    }

    /*
        MATCH (c:Class{is_service:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'})
        WHERE m.number_of_instructions > high_noi
            AND m.cyclomatic_complexity > high_cc
        RETURN m.app_key as app_key,m.cyclomatic_complexity as cyclomatic_complexity,
            m.number_of_instructions as number_of_instructions

        details -> m.full_name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        String query = getHSSNodes(PROPERTIES.get("Heavy_class_high_noi"),
                PROPERTIES.get("Heavy_class_high_cc"));
        query += " RETURN m.app_key as app_key,m.cyclomatic_complexity as cyclomatic_complexity,\n" +
                "m.number_of_instructions as number_of_instructions";
        if (details) {
            query += ",m.full_name as full_name";
        }
        return query;
    }

    private String getHSSNodes(double noiThreshold, double ccThreshold) {
        return " MATCH (c:Class{is_service:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'})\n" +
                "WHERE m.number_of_instructions > " + noiThreshold + "\n" +
                "   AND m.cyclomatic_complexity > " + ccThreshold + "\n";
    }

}
