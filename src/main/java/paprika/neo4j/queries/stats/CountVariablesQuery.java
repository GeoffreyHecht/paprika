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

import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

public class CountVariablesQuery extends PaprikaQuery {

    public static final String KEY = "COUNTVAR";

    public CountVariablesQuery(QueryEngine queryEngine) {
        super("COUNT_VARIABLE", queryEngine);
    }

    @Override
    public String getQuery(boolean details) {
        return "MATCH (n:Variable) return n.app_key as app_key, count(n) as nb_variables";
    }

}
