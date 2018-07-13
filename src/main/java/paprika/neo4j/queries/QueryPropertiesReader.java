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

package paprika.neo4j.queries;

import paprika.neo4j.queries.antipatterns.fuzzy.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class QueryPropertiesReader {

    private Properties props;

    public QueryPropertiesReader(String propsArg) throws IOException {
        this.props = new Properties();
        if (propsArg != null) {
            props.load(new FileInputStream(propsArg));
        } else {
            props.load(getClass().getClassLoader().getResourceAsStream("thresholds.properties"));
        }
    }

    public void loadThresholds() {
        BLOBQuery.high_lcom = readDoubleProperty("Blob_high_lcom");
        BLOBQuery.veryHigh_lcom = readDoubleProperty("Blob_veryHigh_lcom");
        BLOBQuery.high_noa = readDoubleProperty("Blob_high_noa");
        BLOBQuery.veryHigh_noa = readDoubleProperty("Blob_veryHigh_noa");
        BLOBQuery.high_nom = readDoubleProperty("Blob_high_nom");
        BLOBQuery.veryHigh_nom = readDoubleProperty("Blob_veryHigh_nom");

        CCQuery.high = readDoubleProperty("Class_complexity_high");
        CCQuery.veryHigh = readDoubleProperty("Class_complexity_veryHigh");

        HeavySomethingQuery.high_cc = readDoubleProperty("Heavy_class_high_cc");
        HeavySomethingQuery.veryHigh_cc = readDoubleProperty("Heavy_class_veryHigh_cc");
        HeavySomethingQuery.high_noi = readDoubleProperty("Heavy_class_high_noi");
        HeavySomethingQuery.veryHigh_noi = readDoubleProperty("Heavy_class_veryHigh_noi");

        LMQuery.high = readDoubleProperty("Long_method_noi_high");
        LMQuery.veryHigh  = readDoubleProperty("Long_method_noi_veryHigh");

        SAKQuery.high = readDoubleProperty("SAK_methods_high");
        SAKQuery.veryHigh = readDoubleProperty("SAK_methods_veryHigh");
    }

    private double readDoubleProperty(String property) {
        return Double.valueOf(props.getProperty(property));
    }


}
