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

package paprika.query.neo4j.queries.antipatterns.fuzzy;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import paprika.query.neo4j.queries.QueryPropertiesReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Geoffrey Hecht on 14/08/15.
 */
public class BLOB extends FuzzyQuery {

    public static final String KEY = "BLOB";

    public BLOB(QueryPropertiesReader reader) {
        super(KEY, "Blob.fcl", reader);
    }

    /*
        MATCH (cl:Class)
        WHERE cl.lack_of_cohesion_in_methods > veryHigh_lcom
            AND cl.number_of_methods > veryHigh_nom
            AND cl.number_of_attributes > veryHigh_noa
        RETURN cl.app_key as app_key

        details -> cl.name as full_name,
        else -> count(cl) as BLOB
     */

    @Override
    public String getQuery(boolean details) {
        String query = getBLOBNodes(reader.get("Blob_veryHigh_lcom"), reader.get("Blob_veryHigh_nom"),
                reader.get("Blob_veryHigh_noa"));
        query += "RETURN cl.app_key as app_key,";
        if (details) {
            query += "cl.name as full_name";
        } else {
            query += "count(cl) as BLOB";
        }
        return query;
    }

    /*
        MATCH (cl:Class)
        WHERE cl.lack_of_cohesion_in_methods > high_lcom
            AND cl.number_of_methods > high_nom
            AND cl.number_of_attributes > high_noa
        RETURN cl.app_key as app_key, cl.lack_of_cohesion_in_methods as lack_of_cohesion_in_methods,
            cl.number_of_methods as number_of_methods, cl.number_of_attributes as number_of_attributes

        details -> cl.name as full_name
     */

    @Override
    public String getFuzzyQuery(boolean details) {
        String query = getBLOBNodes(reader.get("Blob_high_lcom"), reader.get("Blob_high_nom"),
                reader.get("Blob_high_noa"));
        query += "RETURN cl.app_key as app_key, cl.lack_of_cohesion_in_methods as lack_of_cohesion_in_methods,\n" +
                " cl.number_of_methods as number_of_methods, cl.number_of_attributes as number_of_attributes";
        if (details) {
            query += ",cl.name as full_name";
        }
        return query;
    }

    private String getBLOBNodes(double lcomThreshold, double nomThreshold, double noaThreshold) {
        return "MATCH (cl:Class)\n" +
                "WHERE cl.lack_of_cohesion_in_methods >" + lcomThreshold + "\n" +
                "   AND cl.number_of_methods > " + nomThreshold + "\n" +
                "   AND cl.number_of_attributes > " + noaThreshold + "\n";
    }

    @Override
    public List<Map<String, Object>> getFuzzyResult(List<Map<String, Object>> result, FIS fis) {
        int lcom;
        int noa;
        int nom;
        List<Map<String, Object>> fuzzyResult = new ArrayList<>();
        FunctionBlock fb = fis.getFunctionBlock(null);
        for (Map<String, Object> res : result) {
            lcom = (int) res.get("lack_of_cohesion_in_methods");
            noa = (int) res.get("number_of_attributes");
            nom = (int) res.get("number_of_methods");
            if (lcom >= reader.get("Blob_veryHigh_lcom") && noa >= reader.get("Blob_veryHigh_noa")
                    && nom >= reader.get("Blob_veryHigh_nom")) {
                res.put("fuzzy_value", 1);
            } else {
                fb.setVariable("lack_of_cohesion_in_methods", lcom);
                fb.setVariable("number_of_attributes", noa);
                fb.setVariable("number_of_methods", nom);
                fb.evaluate();
                res.put("fuzzy_value", fb.getVariable("res").getValue());
            }
            fuzzyResult.add(res);
        }
        return fuzzyResult;
    }


}
