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
import paprika.TestUtil;
import paprika.launcher.arg.Argument;
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.neo4j.queries.AnalyzedApkTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static paprika.launcher.PaprikaLauncherTest.PLATFORMS_PATH;

public class PaprikaArgParserTest {

    public static final String APK_FOLDER = "/apk";
    public static final String RECURSIVE_SEARCH_TEST_PATH = "/argparser";

    private TestUtil util = new TestUtil();

    private String[] getDefaultTestArgs() {
        return new String[]{"analyse", "-a", util.getPath(PLATFORMS_PATH),
                "-db", util.getPath(AnalyzedApkTest.DB_PATH), "-omp",
                util.getPath(APK_FOLDER)
        };
    }

    private PaprikaArgParser loadStandardArgs() throws Exception {
        PaprikaArgParser parser = new PaprikaArgParser();
        parser.parseArgs(getDefaultTestArgs());
        return parser;
    }


    @Test
    public void readArgsTest() throws Exception {
        PaprikaArgParser parser = new PaprikaArgParser();
        parser.parseArgs(getDefaultTestArgs());
        assertThat(parser.getArg(Argument.DATABASE_ARG), is(util.getPath(AnalyzedApkTest.DB_PATH)));
        assertThat(parser.getFlagArg(Argument.ONLY_MAIN_PACKAGE_ARG), is(true));
        String[] otherArgs = {
                "analyse", "-a", util.getPath(PLATFORMS_PATH), "-r", "6.5", "-tsdk", "96",
                "-db", util.getPath(AnalyzedApkTest.DB_PATH), util.getPath(APK_FOLDER)
        };
        parser = new PaprikaArgParser();
        parser.parseArgs(otherArgs);
        assertThat(parser.getFlagArg(Argument.ONLY_MAIN_PACKAGE_ARG), is(false));
        assertThat(parser.getDoubleArg(Argument.RATING_ARG), is(closeTo(6.5, 0.1)));
        assertThat(parser.getIntArg(Argument.TARGET_SDK_VERSION_ARG), is(96));
    }

    private static final List<String> CONTENTS = Arrays.asList(
            "dosbox.apk", "opengpx.apk", "openmanager.apk", "passandroid.apk", "tint.apk",
            "tof.apk", "wikipedia.apk", "witness.apk", "wordpress.apk"
    );

    private boolean contentsAreAPKs(List<String> got) {
        assertThat(got.size(), is(CONTENTS.size()));
        for (String apk : CONTENTS) {
            boolean found = false;
            for (String item : got) {
                if (item.contains(apk)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void recursiveFolderSearch() throws Exception {
        PaprikaArgParser parser = new PaprikaArgParser();
        String[] args = {"analyse", "-a", util.getPath(PLATFORMS_PATH),
                "-db", util.getPath(AnalyzedApkTest.DB_PATH), "-omp",
                util.getPath(RECURSIVE_SEARCH_TEST_PATH)
        };
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
        assertTrue(contentsAreAPKs(got));
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
