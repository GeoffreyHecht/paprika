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

package paprika.neo4j.queries.antipatterns;

import org.junit.jupiter.api.Test;
import paprika.neo4j.queries.AnalyzedApkTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NLMRTest extends AnalyzedApkTest {

    private List<String> expectedInClasses = Arrays.asList(
            getWitnessClass("activities.MainActivity"),
            getWitnessClass("activities.CloneActivity"),
            getWitnessClass("App"),
            getWitnessClass("heavy.HeavyService"),
            getWitnessClass("heavy.HeavyIntentService"),
            getWitnessClass("BadContentProvider")
    );

    private List<String> absentInClasses = Arrays.asList(
            getWitnessClass("activities.AActivity"), // onTrimMemory
            getWitnessClass("activities.AnotherActivity"), // onLowMemory
            getWitnessClass("activities.OtherActivity") // inheritance
    );

    @Test
    public void checkNLMRResults() {
        List<Map<String, Object>> results = filterResult(engine.execute(new NLMRQuery().getQuery(true)),
                DEFAULT_VERSION, DEFAULT_KEY_LABEL);
        for (String aClass : expectedInClasses) {
            assertTrue(foundInClass(results, aClass), aClass);
        }
        for (String aClass : absentInClasses) {
            assertFalse(foundInClass(results, aClass), aClass);
        }
    }

}
