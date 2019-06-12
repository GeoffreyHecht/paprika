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

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Used to read and fetch the properties in fuzzy queries from a .properties file.
 * All properties included in the file will automatically be loaded.
 * All properties must be able to be interpreted as a double.
 */
public class QueryPropertiesReader {

    public static final String DEFAULT_PROPS = "thresholds.properties";

    private Map<String, Double> properties = new HashMap<>();

    /**
     * Load fuzzy properties from a file.
     *
     * @param propsArg the path to the file, or null if using the default jar properties
     * @throws IOException              if failing to find or read the file
     * @throws QueryPropertiesException if one of the properties cannot be parse as a double
     */
    public void loadProperties(@Nullable String propsArg) throws IOException, QueryPropertiesException {
        Properties props = new Properties();
        if (propsArg != null) {
            props.load(new FileInputStream(propsArg));
        } else {
            props.load(QueryPropertiesReader.class
                    .getClassLoader().getResourceAsStream(DEFAULT_PROPS));
        }
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            try {
                properties.put(entry.getKey().toString(), Double.valueOf(entry.getValue().toString()));
            } catch (NumberFormatException e) {
                throw new QueryPropertiesException(propsArg, entry.getKey().toString(), e);
            }
        }
    }

    /**
     * Replace all String occurrences of one of the properties name by its value in a given String.
     * The method {@link #loadProperties(String)} must be called beforehand.
     * For instance, the String (Class_complexity_high, 0) will be converted to (28, 0).
     *
     * @param function the text with the properties names to replace
     * @return the same text with the properties names as values
     */
    public String replaceProperties(String function) {
        for (Map.Entry<String, Double> entry : properties.entrySet()) {
            if (function.contains(entry.getKey())) {
                function = function.replaceAll(entry.getKey(), Double.toString(entry.getValue()));
            }
        }
        return function;
    }

    /**
     * Fetch a loaded property from its name.
     *
     * @param name the name of the property
     * @return the double value of the property, or null if not found
     * @throws NullPointerException if properties wre not loaded earlier
     */
    public double get(String name) {
        return properties.get(name);
    }

}
