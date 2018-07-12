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
import paprika.entities.PaprikaApp;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import java.util.Arrays;

import static org.neo4j.cypherdsl.CypherQuery.*;

public class PropertyQuery extends PaprikaQuery {

    public static final String ALL_LCOM = "ALLLCOM";
    public static final String ALL_CYCLOMATIC = "ALLCYCLO";
    public static final String ALL_CC = "ALLCC";
    public static final String ALL_METHODS = "ALLNUMMETHODS";

    private String property;
    private String nodeType;

    public PropertyQuery(String queryName, QueryEngine queryEngine, String nodeType, String property) {
        super(queryName, queryEngine);
        this.property = property;
        this.nodeType = nodeType;
    }

    /*
        MATCH (n:nodeType)
        RETURN n.app_key as app_key, n.name as name, n.property as property
     */

    @Override
    public String getQuery(boolean details) {
        Identifier identifier = identifier("n");

        return match(identifier.label(nodeType))
                .returns(Arrays.asList(
                        as(identifier.property(PaprikaApp.APP_KEY), "app_key"),
                        as(identifier.property(PaprikaApp.NAME), "name"),
                        as(identifier.property(property), property)
                )).toString();
    }

}
