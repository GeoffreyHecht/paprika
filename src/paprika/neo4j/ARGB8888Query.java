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

package paprika.neo4j;

import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;

/**
 * Created by antonin on 16-05-03.
 */
public class ARGB8888Query extends Query {

    private ARGB8888Query(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static ARGB8888Query createARGB8888Query(QueryEngine queryEngine) {
        return new ARGB8888Query(queryEngine);
    }

    @Override
    public void execute(boolean details) throws CypherException, IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (e: ExternalArgument) WHERE exists(e.is_argb_8888) RETURN e";
            if (details) {
                query += ", count(e) as ARGB8888";
            }
            Result result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_ARGB8888.csv");
        }
    }

}
