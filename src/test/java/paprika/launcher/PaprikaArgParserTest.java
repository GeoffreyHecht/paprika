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
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.neo4j.queries.AnalyzedApkTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static paprika.launcher.PaprikaLauncherTest.PLATFORMS_PATH;

public class PaprikaArgParserTest {

    public static final String APK_FOLDER = "/apk";
    public static final String RECURSIVE_SEARCH_TEST_PATH = "/argparser";

    private static final List<String> CONTENTS = Arrays.asList(
            "dosbox.apk", "opengpx.apk", "openmanager.apk", "passandroid.apk", "tint.apk",
            "tof.apk", "wikipedia.apk", "witness.apk", "wordpress.apk"
    );

    private PaprikaArgParser loadStandardArgs() throws Exception {
        String[] args = {"analyse", "-a", getClass().getResource(PLATFORMS_PATH).getFile(),
                "-db", getClass().getResource(AnalyzedApkTest.DB_PATH).getFile(), "-omp",
                getClass().getResource(APK_FOLDER).getFile()
        };
        PaprikaArgParser parser = new PaprikaArgParser();
        parser.parseArgs(args);
        return parser;
    }

    @Test
    public void getAPKsInFolderTest() throws Exception {
        PaprikaArgParser parser = loadStandardArgs();
        List<String> got = parser.getAppsPaths();
        assertThat(got.size(), is(greaterThanOrEqualTo(9)));
        String prefix = getClass().getResource(APK_FOLDER).getFile() + "/";
        for (String item : CONTENTS) {
            assertThat(got, hasItem(prefix + item));
        }
    }

    @Test
    public void recursiveFolderSearch() throws Exception {
        String[] args = {"analyse", "-a", getClass().getResource(PLATFORMS_PATH).getFile(),
                "-db", getClass().getResource(AnalyzedApkTest.DB_PATH).getFile(), "-omp",
                getClass().getResource(RECURSIVE_SEARCH_TEST_PATH).getFile()
        };
        PaprikaArgParser parser = new PaprikaArgParser();
        parser.parseArgs(args);
        List<String> got = parser.getAppsPaths();
        assertThat(got.size(), is(equalTo(2)));
        for (String item : got) {
            assertThat(item, anyOf(containsString("test_apk_1.apk"),
                    containsString("test_apk_2.apk")));
        }
    }


    @Test
    public void shaTest() throws Exception {
        String sha = new PaprikaArgParser().computeSha256(
                getClass().getResource(APK_FOLDER + "/" + "dosbox.apk").getFile());
        assertThat(sha, is(equalTo("7edc9be5d5d97d612eaa7e8c07593e2de20d7239ebde0742d32c1bb1f9fc7018")));
    }

    @Test
    public void getAppsPathTest() throws Exception {
        PaprikaArgParser parser = loadStandardArgs();
        List<String> got = parser.getAppsPaths();
        List<String> expected = CONTENTS.stream()
                .map(item -> getClass().getResource(APK_FOLDER).getFile() + "/" + item)
                .collect(Collectors.toList());
        assertThat(got.size(), is(equalTo(expected.size())));
        for (String element : expected) {
            assertThat(got, hasItem(element));
        }
    }

    @Test
    public void getAppsNamesTest() throws Exception {
        PaprikaArgParser parser = loadStandardArgs();
        List<String> got = parser.getAppsFilenames(parser.getAppsPaths());
        List<String> expected = CONTENTS.stream()
                .map(item -> item.substring(0, item.length() - 4))
                .collect(Collectors.toList());
        assertThat(got.size(), is(equalTo(expected.size())));
        for (String element : expected) {
            assertThat(got, hasItem(element));
        }
    }

}
