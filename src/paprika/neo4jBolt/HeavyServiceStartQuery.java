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
public class HeavyServiceStartQuery extends FuzzyQuery{
    protected static double high_cc = 3.5;
    protected static double veryHigh_cc = 5;
    protected static double high_noi = 17;
    protected static double veryHigh_noi = 26;



    private HeavyServiceStartQuery(QueryEngineBolt queryEngine) {
        super(queryEngine);
        fclFile = "/HeavySomething.fcl";
    }

    public static HeavyServiceStartQuery createHeavyServiceStartQuery(QueryEngineBolt queryEngine) {
        return new HeavyServiceStartQuery(queryEngine);
    }

    @Override
    public void execute(boolean details) throws IOException {
        StatementResult result;
        try (Transaction tx = this.session.beginTransaction()) {
            String query = "MATCH (c:Class{is_service:true ,app_key:"+queryEngine.getKeyApp()+"})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'}) WHERE m.number_of_instructions > "+veryHigh_noi+" AND m.cyclomatic_complexity>"+veryHigh_cc+" return m as nod,m.app_key as app_key";
            if(details){
                query += ",m.full_name as full_name";
            }else{
                query += ",count(m) as HSS";
            }
            result = tx.run(query);
            queryEngine.resultToCSV(result,"HSS_NO_FUZZY");
            tx.success();
        }
    }
    @Override
    public void executeFuzzy(boolean details) throws IOException {
    	StatementResult result;
            try (Transaction tx = this.session.beginTransaction()) {
                String query = "MATCH (c:Class{is_service:true ,app_key:"+queryEngine.getKeyApp()+"})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onStartCommand'}) WHERE m.number_of_instructions > "+high_noi+"  AND m.cyclomatic_complexity>"+high_cc+" return m as nod,m.app_key as app_key,m.cyclomatic_complexity as cyclomatic_complexity, m.number_of_instructions as number_of_instructions";
                if(details){
                    query += ",m.full_name as full_name";
                }
                result = tx.run(query);
                int noi;
                int cc;
                List<Map> fuzzyResult = new ArrayList<>();
                FunctionBlock fb = this.fuzzyFunctionBlock();
                while(result.hasNext()){
                    Map<String, Object> res = new HashMap<>(result.next().asMap());
                    cc =((Long)res.get("cyclomatic_complexity")).intValue();
                    noi =((Long)res.get("number_of_instructions")).intValue();

                    if(cc >= veryHigh_cc && noi >= veryHigh_noi){
                        res.put("fuzzy_value", 1);
                    }else {
                        fb.setVariable("cyclomatic_complexity",cc);
                        fb.setVariable("number_of_instructions",noi);
                        fb.evaluate();
                        res.put("fuzzy_value", fb.getVariable("res").getValue());
                    }
                    fuzzyResult.add(res);
                    }
                    queryEngine.resultToCSV(fuzzyResult,"HSS");
                    tx.success();
            }
    }


}
