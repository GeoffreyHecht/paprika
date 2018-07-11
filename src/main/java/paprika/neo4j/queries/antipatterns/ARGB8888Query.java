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

package paprika.neo4j.queries.antipatterns;

import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.expression.Expression;
import paprika.entities.PaprikaExternalArgument;
import paprika.metrics.arg.IsARGB8888;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.EXTERNAL_ARGUMENT_TYPE;

/**
 * Created by antonin on 16-05-03.
 */
public class ARGB8888Query extends PaprikaQuery {

    public static final String KEY = "ARGB8888";

    public ARGB8888Query(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
     * MATCH (e: ExternalArgument)
     * WHERE exists(e.is_argb_8888)
     * RETURN e.app_key AS app_key, e.name AS name
     *
     * details -> count(e) AS ARGB8888
     */

    @Override
    public String getQuery(boolean details) {
        Identifier externalArg = identifier("e");

        List<Expression> returns = new ArrayList<>();
        returns.add(as(externalArg.property(PaprikaExternalArgument.APP_KEY), "app_key"));
        returns.add(as(externalArg.property(PaprikaExternalArgument.NAME), "name"));
        if (details) {
            returns.add(as(count(externalArg), KEY));
        }

        return match(node(externalArg).label(EXTERNAL_ARGUMENT_TYPE))
                .where(exists(externalArg.property(IsARGB8888.NAME)))
                .returns(returns)
                .toString();
    }

}
