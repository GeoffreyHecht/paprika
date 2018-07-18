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

package paprika.neo4j.queries.stats;

import paprika.neo4j.queries.PaprikaQuery;

public class CountInnerQuery extends PaprikaQuery {

    public static final String COMMAND_KEY = "COUNTINNER";

    public CountInnerQuery() {
        super("COUNT_INNER");
    }

    /*
        MATCH (n:Class) WHERE exists(n.is_inner_class)
        RETURN n.app_key as app_key,count(n) as nb_inner_classes
     */

    @Override
    public String getQuery(boolean details) {
        return " MATCH (n:Class) WHERE exists(n.is_inner_class)\n" +
                "RETURN n.app_key as app_key,count(n) as nb_inner_classes";
    }

}
