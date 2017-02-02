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
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class InvalidateWithoutRectQuery extends Query {

    private InvalidateWithoutRectQuery(QueryEngine queryEngine) {
        super(queryEngine);
    }

    public static InvalidateWithoutRectQuery createInvalidateWithoutRectQuery(QueryEngine queryEngine) {
        return new InvalidateWithoutRectQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query ="MATCH (:Class{parent_name:'android.view.View'})-[:CLASS_OWNS_METHOD]->(n:Method{name:'onDraw'})-[:CALLS]->(e:ExternalMethod{name:'invalidate'}) WHERE NOT (e)-[:METHOD_OWNS_ARGUMENT]->(:ExternalArgument) return n.app_key";
            if(details){
                query += ",n.full_name as full_name";
            }else{
                query += ",count(n) as IWR";
            }
            result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result, "_IWR.csv");
        }
    }

}
