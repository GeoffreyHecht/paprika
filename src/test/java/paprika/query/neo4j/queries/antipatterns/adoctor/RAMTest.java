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

package paprika.query.neo4j.queries.antipatterns.adoctor;

import org.junit.jupiter.api.Test;
import paprika.query.neo4j.queries.AnalyzedApkTest;

import java.util.List;
import java.util.Map;

public class RAMTest extends AnalyzedApkTest {

    private String[][] expectedBefore19 = {
            {"aRAMCall", getWitnessClass("views.ABoringView")},
    };

    private String[][] empty = {};

    @Test
    public void checkRAMResults() {
        List<Map<String, Object>> results = engine.execute(new RigidAlarmManager().getQuery(true));
        List<Map<String, Object>> before = filterResult(results, 15);
        checkMethodEntries(before, expectedBefore19, empty);
        List<Map<String, Object>> after = filterResult(results, DEFAULT_VERSION);
        checkMethodEntries(after, empty, expectedBefore19);
    }
}
