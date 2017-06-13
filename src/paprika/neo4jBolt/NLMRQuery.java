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
public class NLMRQuery extends Query {

    private NLMRQuery(QueryEngineBolt queryEngine) {
        super(queryEngine);
    }

    public static NLMRQuery createNLMRQuery(QueryEngineBolt queryEngine) {
        return new NLMRQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws IOException {
        try (Transaction tx = this.session.beginTransaction()) {
            String query = "MATCH (cl:Class  {app_key:"+queryEngine.getKeyApp()+"}) WHERE EXISTS(cl.is_activity) AND NOT (cl:Class)-[:CLASS_OWNS_METHOD]->(:Method { name: 'onLowMemory' }) AND NOT (cl)-[:EXTENDS]->(:Class) RETURN cl as nod,cl.app_key as app_key";
            if(details){
                query += ",cl.name as full_name";
            }else{
                query += ",count(cl) as NLMR";
            }
            StatementResult result = tx.run(query);
            queryEngine.resultToCSV(result, "NLMR");
            tx.success();
        }
    }
}
