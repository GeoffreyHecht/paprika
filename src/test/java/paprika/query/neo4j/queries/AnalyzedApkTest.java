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

package paprika.query.neo4j.queries;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import paprika.TestUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AnalyzedApkTest {

    public static final String DB_PATH = "/witness/db";

    public static final int DEFAULT_VERSION = 27;

    protected TestEngine engine;
    protected TestUtil util = new TestUtil();

    @BeforeEach
    public void setUp() {
        assertNotNull(util.getPath(DB_PATH), "Failed to find test resources db folder");
        engine = new TestEngine(util.getPath(DB_PATH));
    }

    @AfterEach
    public void tearDown() {
        engine.stop();
    }

    protected List<Map<String, Object>> filterResult(List<Map<String, Object>> original, int version) {
        return original.stream()
                .filter(item -> Integer.valueOf(item.get("app_key").toString()) == version)
                .collect(Collectors.toList());
    }

    protected String getWitnessClass(String name) {
        return "com.antipatterns.app." + name;
    }

    protected boolean resultContains(List<Map<String, Object>> result, String label, String expected) {
        for (Map<String, Object> item : result) {
            if (expected.equals(item.get(label))) {
                return true;
            }
        }
        return false;
    }

    protected boolean foundInClass(List<Map<String, Object>> result, String aClass) {
        return resultContains(result, "full_name", aClass);
    }

    protected boolean foundInMethod(List<Map<String, Object>> result, String method, String aClass) {
        return resultContains(result, "full_name", method + "#" + aClass);
    }

    protected void checkClassEntries(List<Map<String, Object>> results, String[] expected, String[] absent) {
        assertThat(results.size(), is(greaterThanOrEqualTo(expected.length)));
        for (String aClass : expected) {
            assertTrue(foundInClass(results, aClass), aClass);
        }
        for (String aClass : absent) {
            assertFalse(foundInClass(results, aClass), aClass);
        }
    }

    protected void checkMethodEntries(List<Map<String, Object>> results, String[][] expected, String[][] absent) {
        assertThat(results.size(), is(greaterThanOrEqualTo(expected.length)));
        for (String[] entry : expected) {
            assertTrue(foundInMethod(results, entry[0], entry[1]), entry[0] + "#" + entry[1]);
        }
        for (String[] entry : absent) {
            assertFalse(foundInMethod(results, entry[0], entry[1]), entry[0] + "#" + entry[1]);
        }
    }

}
