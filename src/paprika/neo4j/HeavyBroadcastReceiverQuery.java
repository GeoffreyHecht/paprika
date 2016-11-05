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

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import org.neo4j.cypher.CypherException;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class HeavyBroadcastReceiverQuery extends FuzzyQuery{
    protected static double high_cc = 3.5;
    protected static double veryHigh_cc = 5;
    protected static double high_noi = 17;
    protected static double veryHigh_noi = 26;

    private HeavyBroadcastReceiverQuery(QueryEngine queryEngine) {
        super(queryEngine);
        fclFile = "/HeavySomething.fcl";
    }

    public static HeavyBroadcastReceiverQuery createHeavyBroadcastReceiverQuery(QueryEngine queryEngine) {
        return new HeavyBroadcastReceiverQuery(queryEngine);
    }


    public void execute(boolean details) throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (c:Class{is_broadcast_receiver:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onReceive'}) WHERE m.number_of_instructions > "+veryHigh_noi+" AND m.cyclomatic_complexity>"+veryHigh_cc+" return m.app_key as app_key";
            if(details){
                query += ",m.full_name as full_name";
            }else{
                query += ",count(m) as HBR";
            }
            result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result,"_HBR_NO_FUZZY.csv");
        }
    }

    public void executeFuzzy(boolean details) throws CypherException, IOException {
            Result result;
            try (Transaction ignored = graphDatabaseService.beginTx()) {
                String query = "MATCH (c:Class{is_broadcast_receiver:true})-[:CLASS_OWNS_METHOD]->(m:Method{name:'onReceive'}) WHERE m.number_of_instructions > "+high_noi+" AND m.cyclomatic_complexity>"+high_cc+" return m.app_key as app_key,m.cyclomatic_complexity as cyclomatic_complexity, m.number_of_instructions as number_of_instructions";
                if(details){
                    query += ",m.full_name as full_name";
                }
                result = graphDatabaseService.execute(query);
                List<String> columns = new ArrayList<>(result.columns());
                columns.add("fuzzy_value");
                int noi,cc;
                List<Map> fuzzyResult = new ArrayList<>();
                File fcf = new File(fclFile);
                //We look if the file is in a directory otherwise we look inside the jar
                FIS fis;
                if(fcf.exists() && !fcf.isDirectory()){
                    fis = FIS.load(fclFile, false);
                }else{
                    fis = FIS.load(getClass().getResourceAsStream(fclFile),false);
                }
                FunctionBlock fb = fis.getFunctionBlock(null);
                while(result.hasNext()){
                    HashMap res = new HashMap(result.next());
                    cc = (int) res.get("cyclomatic_complexity");
                    noi = (int) res.get("number_of_instructions");
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
                    queryEngine.resultToCSV(fuzzyResult,columns,"_HBR.csv");
            }
    }


}
