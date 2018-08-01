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

package paprika.query.neo4j.queries.antipatterns.ui;

import paprika.query.neo4j.queries.PaprikaQuery;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class Overdraw extends PaprikaQuery {

    public static final String KEY = "UIO";

    public Overdraw() {
        super(KEY);
    }

    /*
        MATCH (:Class{parent_name:"android.view.View"})-[:CLASS_OWNS_METHOD]->(n:Method{name:"onDraw"})
            -[:METHOD_OWNS_ARGUMENT]->(:Argument{position:1,name:"android.graphics.Canvas"})
        WHERE NOT (n)-[:CALLS]->(:ExternalMethod{full_name:"clipRect#android.graphics.Canvas"})
            AND NOT (n)-[:CALLS]->(:ExternalMethod{full_name:"quickReject#android.graphics.Canvas"})
            AND NOT (n)-[:CALLS]->(:ExternalMethod{full_name:"clipOutRect#android.graphics.Canvas"})
        RETURN n.app_key as app_key

        details -> n.full_name as full_name
        else -> count(n) as UIO
     */

    @Override
    public String getQuery(boolean details) {
        String query = "MATCH (:Class{parent_name:\"android.view.View\"})-[:CLASS_OWNS_METHOD]->(n:Method{name:\"onDraw\"})\n" +
                "            -[:METHOD_OWNS_ARGUMENT]->(:Argument{position:1,name:\"android.graphics.Canvas\"})\n" +
                "WHERE NOT (n)-[:CALLS]->(:ExternalMethod{full_name:\"clipRect#android.graphics.Canvas\"})\n" +
                "   AND NOT (n)-[:CALLS]->(:ExternalMethod{full_name:\"quickReject#android.graphics.Canvas\"})\n" +
                "   AND NOT (n)-[:CALLS]->(:ExternalMethod{full_name:\"clipOutRect#android.graphics.Canvas\"})\n" +
                "RETURN n.app_key as app_key,";
        if (details) {
            query += "n.full_name as full_name";
        } else {
            query += "count(n) as UIO";
        }

        return query;
    }

}
