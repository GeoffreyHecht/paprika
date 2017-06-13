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

package paprika.neo4jBolt;

import java.io.IOException;


import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class InvalidateWithoutRectQuery extends Query {

    private InvalidateWithoutRectQuery(QueryEngineBolt queryEngine) {
        super(queryEngine);
    }

    public static InvalidateWithoutRectQuery createInvalidateWithoutRectQuery(QueryEngineBolt queryEngine) {
        return new InvalidateWithoutRectQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws IOException {
        StatementResult result;
        try (Transaction tx = this.session.beginTransaction()) {
            String query ="MATCH (:Class{parent_name:'android.view.View',app_key:"+queryEngine.getKeyApp()+"})-[:CLASS_OWNS_METHOD]->(n:Method{name:'onDraw'})-[:CALLS]->(e:ExternalMethod{name:'invalidate'}) WHERE NOT (e)-[:METHOD_OWNS_ARGUMENT]->(:ExternalArgument)  return n as nod,n.app_key";
            if(details){
                query += ",n.full_name as full_name";
            }else{
                query += ",count(n) as IWR";
            }
            result = tx.run(query);
            queryEngine.resultToCSV(result, "IWR");
            tx.success();
        }
    }

}
