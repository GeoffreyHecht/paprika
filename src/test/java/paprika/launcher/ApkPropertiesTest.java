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

package paprika.launcher;

import org.junit.jupiter.api.Test;
import paprika.analyse.ApkPropertiesParser;
import paprika.analyse.PropertiesException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static paprika.launcher.arg.Argument.*;

public class ApkPropertiesTest {

    public static final String JSON_FOLDER = "/json/";

    private String firstApk = "test_apk_1";
    private String otherApk = "test_apk_2";

    private PrintStream silent = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            // NO-OP
        }
    });

    private ApkPropertiesParser setupAndRead(String json)
            throws PropertiesException, IOException {
        ApkPropertiesParser parser = new ApkPropertiesParser(silent,
                getClass().getResource(JSON_FOLDER).getFile() + json, Arrays.asList(firstApk, otherApk));
        parser.readProperties();
        return parser;
    }

    @Test
    public void standardSyntaxTest() throws Exception {
        ApkPropertiesParser parser = setupAndRead("standard.json");
        assertThat(parser.getAppProperty(firstApk, NAME_ARG.toString()), is(equalTo("myCustomName")));
        assertThat(parser.getAppProperty(firstApk, PACKAGE_ARG.toString()), is(equalTo("custom.package")));
        assertThat(parser.getAppProperty(firstApk, KEY_ARG.toString()), is(equalTo("customKey")));
        assertThat(parser.getAppProperty(firstApk, DEVELOPER_ARG.toString()), is(equalTo("myDev")));
        assertThat(parser.getAppProperty(firstApk, CATEGORY_ARG.toString()), is(equalTo("myCustomCategory")));
        assertThat(parser.getAppProperty(firstApk, NB_DOWNLOAD_ARG.toString()), is(equalTo("58")));
        assertThat(parser.getAppProperty(firstApk, DATE_ARG.toString()), is(equalTo("customDate")));
        assertThat(parser.getAppProperty(firstApk, RATING_ARG.toString()), is(equalTo("3.0")));
        assertThat(parser.getAppProperty(firstApk, SIZE_ARG.toString()), is(equalTo("1556")));
        assertThat(parser.getAppProperty(firstApk, VERSION_CODE_ARG.toString()), is(equalTo("5.6")));
        assertThat(parser.getAppProperty(firstApk, VERSION_NAME_ARG.toString()), is(equalTo("myVN")));
        assertThat(parser.getAppProperty(firstApk, TARGET_SDK_VERSION_ARG.toString()), is(equalTo("89")));
        assertThat(parser.getAppProperty(firstApk, SDK_VERSION_ARG.toString()), is(equalTo("55")));
        assertThat(parser.getAppProperty(firstApk, PRICE_ARG.toString()), is(equalTo("25")));

        assertThat(parser.getAppProperty(otherApk, PACKAGE_ARG.toString()), is(equalTo("other.package")));
        assertThat(parser.getAppProperty(otherApk, KEY_ARG.toString()), is(equalTo("otherKey")));
    }

    @Test
    public void alternativeSyntaxTest() throws Exception {
        ApkPropertiesParser parser = setupAndRead("alternative.json");
        assertThat(parser.getAppProperty(firstApk, NAME_ARG.toString()), is(equalTo("myCustomName")));
        assertThat(parser.getAppProperty(firstApk, PACKAGE_ARG.toString()), is(equalTo("custom.package")));
        assertThat(parser.getAppProperty(firstApk, KEY_ARG.toString()), is(equalTo("customKey")));

        assertThat(parser.getAppProperty(otherApk, NAME_ARG.toString()), is(equalTo("myCustomName")));
        assertThat(parser.getAppProperty(otherApk, PACKAGE_ARG.toString()), is(equalTo("other.package")));
        assertThat(parser.getAppProperty(otherApk, KEY_ARG.toString()), is(equalTo("otherKey")));
    }

    @Test
    public void mixedSyntaxTest() throws Exception {
        ApkPropertiesParser parser = setupAndRead("mixed.json");
        assertThat(parser.getAppProperty(firstApk, NAME_ARG.toString()), is(equalTo("myCustomName")));
        assertThat(parser.getAppProperty(firstApk, PACKAGE_ARG.toString()), is(equalTo("override.package")));
        assertThat(parser.getAppProperty(firstApk, KEY_ARG.toString()), is(equalTo("customKey")));

        assertThat(parser.getAppProperty(otherApk, NAME_ARG.toString()), is(equalTo("OverrideName")));
        assertThat(parser.getAppProperty(otherApk, PACKAGE_ARG.toString()), is(equalTo("other.package")));
        assertThat(parser.getAppProperty(otherApk, KEY_ARG.toString()), is(equalTo("otherKey")));
    }

}
