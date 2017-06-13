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
import org.neo4j.driver.v1.Transaction;/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class MIMQuery extends Query {

    private MIMQuery(QueryEngineBolt queryEngine) {
        super(queryEngine);
    }

    public static MIMQuery createMIMQuery(QueryEngineBolt queryEngine) {
        return new MIMQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws IOException {
        try (Transaction tx = this.session.beginTransaction()) {
            String query = "MATCH (m1:Method  {app_key:"+queryEngine.getKeyApp()+"}) WHERE m1.number_of_callers>0   AND NOT EXISTS(m1.is_static) AND NOT EXISTS(m1.is_override) AND NOT (m1)-[:USES]->(:Variable) AND NOT (m1)-[:CALLS]->(:ExternalMethod) AND NOT (m1)-[:CALLS]->(:Method) AND NOT EXISTS(m1.is_init)  RETURN m1 as nod,m1.app_key as app_key";
            if(details){
                query += ",m1.full_name as full_name";
            }else{
                query += ",count(m1) as MIM";
            }
            StatementResult result = tx.run(query);
            queryEngine.resultToCSV(result, "MIM");
            tx.success();
        }
    }
}
