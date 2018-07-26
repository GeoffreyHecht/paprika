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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class InvalidateWithoutRectTest extends AnalyzedApkTest {

    private String[][] expectedInMethods = {
            {"onDraw", getWitnessClass("views.NotSoGoodView")}
    };

    private String[][] absentInMethods = {
            {"onCreate", getWitnessClass("activities.MainActivity")},
            {"onDraw", getWitnessClass("views.ABoringView")},
            {"onDraw", getWitnessClass("views.AQuiteGoodView")},
    };


    @Test
    public void checkIWRResults() {
        List<Map<String, Object>> unfiltered = engine.execute(new InvalidateWithoutRectQuery().getQuery(true));
        List<Map<String, Object>> version14 = filterResult(unfiltered, 13, "n.app_key");
        checkMethodEntries(version14, expectedInMethods, absentInMethods);
        List<Map<String, Object>> outdatedAfter = filterResult(unfiltered, DEFAULT_VERSION, "n.app_key");
        assertThat(outdatedAfter.size(), is(equalTo(0)));
    }

}
