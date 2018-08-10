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

package paprika.analyse;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static paprika.launcher.arg.Argument.*;

/**
 * Reads app properties from a properly formatted json file.
 * See the Readme for details on the json format.
 */
public class ApkPropertiesParser {

    public static final String PROPS_FILENAME = "apk-properties.json";

    private static final List<String> PROPS_KEYS = Arrays.asList(
            NAME_ARG.toString(), PACKAGE_ARG.toString(), KEY_ARG.toString(), DEVELOPER_ARG.toString(), CATEGORY_ARG.toString(),
            NB_DOWNLOAD_ARG.toString(), DATE_ARG.toString(), RATING_ARG.toString(), SIZE_ARG.toString(), VERSION_CODE_ARG.toString(),
            VERSION_NAME_ARG.toString(), TARGET_SDK_VERSION_ARG.toString(), SDK_VERSION_ARG.toString(),
            ONLY_MAIN_PACKAGE_ARG.toString(), PRICE_ARG.toString()
    );

    private Map<String, Map<String, String>> appProperties;
    private String jsonPath;
    private PrintStream out;

    /**
     * Constructor.
     *
     * @param out      a stream used for user feedback
     * @param jsonPath the path to the json properties file
     * @param apps     the list of filenames of all the apks
     * @throws PropertiesException if one of the apps has an invalid name
     *                             (like "rating" or another Paprika app property)
     */
    public ApkPropertiesParser(PrintStream out, String jsonPath, List<String> apps) throws PropertiesException {
        this.jsonPath = jsonPath;
        this.out = out;
        appProperties = new HashMap<>();
        for (String app : apps) {
            if (PROPS_KEYS.contains(app)) {
                throw new PropertiesException("'" + app + ".apk' is not a valid apk name for the properties json, " +
                        "please rename the file");
            }
            appProperties.put(app, new HashMap<>());
        }
    }

    /**
     * Loads the properties from the json file.
     *
     * @throws IOException         if failing to read or open the file
     * @throws PropertiesException if trying to set the property of an apk that does not exist
     */
    public void readProperties() throws IOException, PropertiesException {
        File jsonFile = new File(jsonPath);
        if (!jsonFile.exists()) {
            out.println("No " + PROPS_FILENAME + " found.");
            return;
        } else {
            out.println("Parsing " + PROPS_FILENAME + "...");
        }
        JsonObject root = Json.parse(new FileReader(jsonFile)).asObject();
        try {
            processAlternativeSyntax(root);
            processBasicSyntax(root);
        } catch (NullPointerException e) {
            throw new PropertiesException(e);
        }
    }

    private void processAlternativeSyntax(JsonObject root) throws PropertiesException {
        for (String key : PROPS_KEYS) {
            JsonValue propsValues = root.get(key);
            if (propsValues == null) {
                continue;
            }
            for (JsonValue propsValuesItem : propsValues.asArray()) {
                // { value:"myOtherCategory", apps:[ "anApk" ] },
                JsonObject valueObject = propsValuesItem.asObject();
                String value = valueObject.get("value").asString();
                for (JsonValue appValue : valueObject.get("apps").asArray()) {
                    insertProperty(appValue.asString(), key, value);
                }
            }
        }
    }

    private void insertProperty(String app, String property, String value) throws PropertiesException {
        Map<String, String> apkProps = appProperties.get(app);
        if (apkProps == null) {
            throw new PropertiesException("The application " + app +
                    " referred to in the JSON was not found in the folder");
        }
        apkProps.put(property, value);
    }

    private void processBasicSyntax(JsonObject root) throws PropertiesException {
        for (String appName : appProperties.keySet()) {
            JsonValue appObjectValue = root.get(appName);
            if (appObjectValue == null) {
                continue;
            }
            JsonObject appObject = appObjectValue.asObject();
            for (String key : PROPS_KEYS) {
                JsonValue propertyValue = appObject.get(key);
                if (propertyValue != null) {
                    insertProperty(appName, key, propertyValue.asString());
                }
            }
        }
    }

    /**
     * Get an app property that was read from the json file.
     * Must be called after {@link #readProperties()}.
     *
     * @param app      the name of the apk
     * @param property the property to fetch: rating, etc.
     * @return the correct property, or null if it was not set in the JSON
     * or null if the app does not match any app
     */
    public String getAppProperty(String app, String property) {
        Map<String, String> requestedProps = appProperties.get(app);
        if (requestedProps == null) {
            return null;
        }
        return requestedProps.get(property);
    }

    /**
     * Checks if an app has properties set in the json file.
     * Must be called after {@link #readProperties()}.
     *
     * @param app the name of the apk
     * @return true if the app has properties in the json, false otherwise
     */
    public boolean hasProperties(String app) {
        Map<String, String> props = appProperties.get(app);
        if (props == null) {
            return false;
        }
        return !props.isEmpty();
    }

}
