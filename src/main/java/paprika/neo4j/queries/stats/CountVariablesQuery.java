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

import org.neo4j.cypherdsl.Identifier;
import paprika.entities.PaprikaVariable;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import java.util.Arrays;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.VARIABLE_TYPE;

public class CountVariablesQuery extends PaprikaQuery {

    public static final String COMMAND_KEY = "COUNTVAR";

    public CountVariablesQuery(QueryEngine queryEngine) {
        super("COUNT_VARIABLE", queryEngine);
    }

    /*
        MATCH (n:Variable)
        RETURN n.app_key as app_key, count(n) as nb_variables
     */

    @Override
    public String getQuery(boolean details) {
        Identifier variable = identifier("n");

        return match(node(variable).label(VARIABLE_TYPE))
                .returns(Arrays.asList(
                        as(variable.property(PaprikaVariable.APP_KEY), "app_key"),
                        as(count(variable), "nb_variables")
                )).toString();
    }

}
