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

import org.junit.jupiter.api.Test;
import paprika.query.neo4j.queries.AnalyzedApkTest;
import paprika.query.neo4j.queries.QueryPropertiesReader;

import java.util.List;
import java.util.Map;

public class HBRTest extends AnalyzedApkTest {

    private String[][] expectedInMethods = {
            {"onReceive", getWitnessClass("heavy.HeavyBroadcastReceiver")},
    };

    private String[][] absentInMethods = {
            {"onReceive", getWitnessClass("heavy.NormalBroadcastReceiver")},
    };

    @Test
    public void checkHBRResults() throws Exception {
        QueryPropertiesReader reader = new QueryPropertiesReader();
        reader.loadProperties(null);
        List<Map<String, Object>> results = filterResult(
                engine.execute(new HeavyBroadcastReceiver(reader).getQuery(true)), DEFAULT_VERSION);
        checkMethodEntries(results, expectedInMethods, absentInMethods);
    }

}
