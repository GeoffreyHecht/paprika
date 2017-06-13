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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import net.sourceforge.jFuzzyLogic.FunctionBlock;
/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class LMQuery extends FuzzyQuery{
    protected static double high = 17;
    protected static double veryHigh = 26;

    private LMQuery(QueryEngineBolt queryEngine) {
        super(queryEngine);
        fclFile = "/LongMethod.fcl";
    }

    public static LMQuery createLMQuery(QueryEngineBolt queryEngine) {
        return new LMQuery(queryEngine);
    }
    @Override
    public void execute(boolean details) throws IOException {
    	StatementResult result;
        try (Transaction tx = this.session.beginTransaction()) {
        	
            String query = "MATCH (m:Method {app_key:"+queryEngine.getKeyApp()+"} ) WHERE m.number_of_instructions >" + veryHigh + " RETURN m as nod,m.app_key as app_key";
            if(details){
                query += ",m.full_name as full_name";
            }else{
                query += ",count(m) as LM";
            }
            result = tx.run(query);
            queryEngine.resultToCSV(result,"LM_NO_FUZZY");
            tx.success();
        }
    }
    @Override
    public void executeFuzzy(boolean details) throws IOException {
    	StatementResult result;
            try (Transaction tx = this.session.beginTransaction()) {
                String query =  "MATCH (m:Method {app_key:"+queryEngine.getKeyApp()+"} ) WHERE m.number_of_instructions >" + high + "  RETURN m as nod,m.app_key as app_key,m.number_of_instructions as number_of_instructions";
                if(details){
                    query += ",m.full_name as full_name";
                }
                result = tx.run(query);

                int cc;

                List<Map> fuzzyResult = new ArrayList<>();
                FunctionBlock fb = this.fuzzyFunctionBlock();
                while(result.hasNext()){
                    Map<String, Object> res = new HashMap<>(result.next().asMap());
                    cc =((Long)res.get("number_of_instructions")).intValue();

                    if(cc >= veryHigh){
                        res.put("fuzzy_value", 1);
                    }else {
                        fb.setVariable("number_of_instructions",cc);
                        fb.evaluate();

                        res.put("fuzzy_value", fb.getVariable("res").getValue());
                    }
                    fuzzyResult.add(res);
                    }
                    queryEngine.resultToCSV(fuzzyResult,"LM");
                    tx.success();
            }
    }
}
