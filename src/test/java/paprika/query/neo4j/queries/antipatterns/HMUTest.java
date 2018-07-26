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

package paprika.query.neo4j.queries.antipatterns;

import org.junit.jupiter.api.Test;
import paprika.query.neo4j.queries.AnalyzedApkTest;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HMUTest extends AnalyzedApkTest {

    @Test
    public void checkHMUResults() {
        List<Map<String, Object>> results = filterResult(engine.execute(new HashMapUsageQuery().getQuery(true)),
                DEFAULT_VERSION, "m." + DEFAULT_KEY_LABEL);
        assertThat(results.size(), is(greaterThanOrEqualTo(2)));
        assertTrue(foundInMethod(results, "onCreate", getWitnessClass("activities.AActivity")));
        assertTrue(foundInMethod(results, "<init>", getWitnessClass("ABigClass")));
    }

}
