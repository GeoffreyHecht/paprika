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

public class QueryPropertiesReader {

    public static final String DEFAULT_PROPS = "thresholds.properties";

    private Map<String, Double> properties = new HashMap<>();

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

    public String replaceProperties(String function) {
        for (String key : properties.keySet()) {
            if (function.contains(key)) {
                function = function.replaceAll(key, Double.toString(properties.get(key)));
            }
        }
        return function;
    }

    public double get(String name) {
        return properties.get(name);
    }

}
