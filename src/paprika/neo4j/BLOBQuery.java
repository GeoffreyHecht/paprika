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
public class BLOBQuery extends FuzzyQuery{
    protected static double high_lcom = 25;
    protected static double veryHigh_lcom = 40;
    protected static double high_noa = 8.5;
    protected static double veryHigh_noa = 13;
    protected static double high_nom = 14.5;
    protected static double veryHigh_nom = 22;



    private BLOBQuery(QueryEngine queryEngine) {
        super(queryEngine);
        fclFile = "/Blob.fcl";
    }

    public static BLOBQuery createBLOBQuery(QueryEngine queryEngine) {
        return new BLOBQuery(queryEngine);
    }

    public void execute(boolean details) throws CypherException, IOException {
        Result result;
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = "MATCH (cl:Class) WHERE cl.lack_of_cohesion_in_methods >" + veryHigh_lcom + " AND cl.number_of_methods > " + veryHigh_nom + " AND cl.number_of_attributes > " + veryHigh_noa + " RETURN cl.app_key as app_key";
            if(details){
                query += ",cl.name as full_name";
            }else{
                query += ",count(cl) as BLOB";
            }
            result = graphDatabaseService.execute(query);
            queryEngine.resultToCSV(result,"_BLOB_NO_FUZZY.csv");
        }
    }

    public void executeFuzzy(boolean details) throws CypherException, IOException {
            Result result;
            try (Transaction ignored = graphDatabaseService.beginTx()) {
                String query = "MATCH (cl:Class) WHERE cl.lack_of_cohesion_in_methods >" + high_lcom + " AND cl.number_of_methods > " + high_nom + " AND cl.number_of_attributes > " + high_noa + " RETURN cl.app_key as app_key,cl.lack_of_cohesion_in_methods as lack_of_cohesion_in_methods,cl.number_of_methods as number_of_methods, cl.number_of_attributes as number_of_attributes";
                if(details){
                    query += ",cl.name as full_name";
                }
                result = graphDatabaseService.execute(query);
                List<String> columns = new ArrayList<>(result.columns());
                columns.add("fuzzy_value");
                int lcom,noa,nom;
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
                    lcom = (int) res.get("lack_of_cohesion_in_methods");
                    noa = (int) res.get("number_of_attributes");
                    nom = (int) res.get("number_of_methods");
                    if(lcom >= veryHigh_lcom && noa >= veryHigh_noa && nom >= veryHigh_nom){
                        res.put("fuzzy_value", 1);
                    }else {
                        fb.setVariable("lack_of_cohesion_in_methods",lcom);
                        fb.setVariable("number_of_attributes",noa);
                        fb.setVariable("number_of_methods",nom);
                        fb.evaluate();
                        res.put("fuzzy_value", fb.getVariable("res").getValue());
                    }
                    fuzzyResult.add(res);
                    }
                    queryEngine.resultToCSV(fuzzyResult,columns,"_BLOB.csv");
            }
    }


}
