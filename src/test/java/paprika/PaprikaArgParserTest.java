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

package paprika;

import org.junit.jupiter.api.Test;
import paprika.neo4j.queries.AnalyzedApkTest;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static paprika.PaprikaLauncherTest.PLATFORMS_PATH;

public class PaprikaArgParserTest {

    @Test
    public void getAPKsInFolderTest() throws Exception {
        String[] args = {"analyse", "-a", getClass().getResource(PLATFORMS_PATH).getFile(),
                "-db", getClass().getResource(AnalyzedApkTest.DB_PATH).getFile(), "-omp",
                getClass().getResource("/apk").getFile()
        };
        PaprikaArgParser parser = new PaprikaArgParser();
        parser.parseArgs(args);
        List<String> got = parser.getAppsPaths();
        assertThat(got.size(), is(greaterThanOrEqualTo(9)));
        String[] contents = {"dosbox.apk", "opengpx.apk", "openmanager.apk", "passandroid.apk", "tint.apk",
                "tof.apk", "wikipedia.apk", "witness.apk", "wordpress.apk"};
        String prefix = getClass().getResource("/apk").getFile() + File.separator;
        for (String item : contents) {
            assertThat(got, hasItem(prefix + item));
        }
    }

    @Test
    public void shaTest() throws Exception {
        String sha = new PaprikaArgParser().computeSha256(
                getClass().getResource("/apk/dosbox.apk").getFile());
        assertThat(sha, is(equalTo("7edc9be5d5d97d612eaa7e8c07593e2de20d7239ebde0742d32c1bb1f9fc7018")));
    }

}
