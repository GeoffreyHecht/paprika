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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paprika.query.neo4j.queries.AnalyzedApkTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UHATest extends AnalyzedApkTest {

    // Method setRasterizer is left untested since it has been removed in later versions of Android.

    private String[][] alwaysExpected = {
            {"UHA_drawVertices", getWitnessClass("UHATest")},
            {"onDraw", getWitnessClass("views.NotSoGoodView")},
            {"UHA_setMaskFilter", getWitnessClass("UHATest")},
            {"UHA_setPathEffect", getWitnessClass("UHATest")},
            {"UHA_setSubPixelText", getWitnessClass("UHATest")},
    };

    private String[][] v10Results = {
            {"UHA_drawPath", getWitnessClass("UHATest")},
    };

    private String[][] v15Results = {
            {"UHA_drawPosText", getWitnessClass("UHATest")},
            {"UHA_drawTextOnPath", getWitnessClass("UHATest")},
            {"UHA_setDrawFilter", getWitnessClass("UHATest")},
            {"UHA_setAntiAlias", getWitnessClass("UHATest")},
    };

    private String[][] v16Results = {
            {"UHA_setFilterBitmap", getWitnessClass("UHATest")},
    };

    private String[][] v17Results = {
            {"UHA_drawBitmapMesh", getWitnessClass("UHATest")},
            {"UHA_setStrokeCap", getWitnessClass("UHATest")},
    };

    private String[][] v22Results = {
            {"UHA_drawPicture", getWitnessClass("UHATest")},
    };

    private Map<Integer, String[][]> expectedResults;

    @BeforeEach
    public void fillResultMap() {
        expectedResults = new HashMap<>();
        expectedResults.put(10, v10Results);
        expectedResults.put(15, v15Results);
        expectedResults.put(16, v16Results);
        expectedResults.put(17, v17Results);
        expectedResults.put(22, v22Results);
    }

    // v15 -> expected = always + v15 + v16 + v17 + v22
    //     -> unexpected = v10

    @Test
    public void checkUHAResults() {
        List<Map<String, Object>> unfiltered = engine.execute(new UHAQuery().getQuery(true));
        for (Map.Entry<Integer, String[][]> entry : expectedResults.entrySet()) {
            int version = entry.getKey();
            List<String[][]> expected = new ArrayList<>();
            expected.add(alwaysExpected);
            List<String[][]> unexpected = new ArrayList<>();
            for (Integer key : expectedResults.keySet()) {
                if (key >= version) {
                    expected.add(expectedResults.get(key));
                } else {
                    unexpected.add(expectedResults.get(key));
                }
            }

            List<Map<String, Object>> filtered = filterResult(unfiltered, version, "m.app_key");
            for (String[][] item : expected) {
                for (String[] str : item) {
                    assertTrue(resultContains(filtered, "full_name", str[0] + "#" + str[1]),
                            version + "--" + str[0] + "#" + str[1]);
                }
            }
            for (String[][] item : unexpected) {
                for (String[] str : item) {
                    assertFalse(resultContains(filtered, "full_name", str[0] + "#" + str[1]),
                            version + "--" + str[0] + "#" + str[1]);
                }
            }
        }
    }


}
