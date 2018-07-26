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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryPropertiesTest {

    private static final String PROPERTIES_FOLDER = "/properties";

    @Test
    public void validProperties() throws Exception {
        QueryPropertiesReader reader = new QueryPropertiesReader();
        reader.loadProperties(getClass().getResource(PROPERTIES_FOLDER + "/correct.properties").getFile());
        assertThat(reader.get("double_props"), is(closeTo(5.6, 0.1)));
        assertThat(reader.get("int_props"), is(closeTo(3, 0.1)));
        String testStr = "whatever double_props bla int_props";
        assertThat(reader.replaceProperties(testStr), is(equalTo("whatever 5.6 bla 3.0")));
    }

    @Test
    public void cantParseProperties() {
        QueryPropertiesReader reader = new QueryPropertiesReader();
        assertThrows(QueryPropertiesException.class, () ->
                reader.loadProperties(getClass().getResource(PROPERTIES_FOLDER + "/invalid.properties").getFile()));
    }

}
