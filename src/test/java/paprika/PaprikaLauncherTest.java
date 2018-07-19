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
import paprika.entities.PaprikaApp;
import paprika.neo4j.queries.AnalyzedApkTest;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PaprikaLauncherTest {

    public static final String PLATFORMS_PATH = "/android-platforms";
    public static final String APK = "/witness/paprika-witness.apk";

    private PrintStream silent = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            // NO-OP
        }
    });

    @Test
    public void minimalArgsTest() throws Exception {
        String[] args = {"analyse", "-a", getClass().getResource(PLATFORMS_PATH).getFile(),
                "-db", getClass().getResource(AnalyzedApkTest.DB_PATH).getFile(), "-omp",
                getClass().getResource(APK).getFile()
        };
        PaprikaLauncher launcher = new PaprikaLauncher(args, silent);
        PaprikaApp app = launcher.analyzeApp(getClass().getResource(APK).getFile(), false, 0);
        assertThat(app.getName(), is(equalTo("paprika-witness")));
        assertThat(app.getPackage(), is(equalTo("com.antipatterns.app")));
        assertThat(app.getTargetSdkVersion(), is(equalTo(AnalyzedApkTest.DEFAULT_VERSION)));
    }

    @Test
    public void allArgsTest() throws Exception {
        String[] args = {"analyse", "-a", getClass().getResource(PLATFORMS_PATH).getFile(),
                "-db", getClass().getResource(AnalyzedApkTest.DB_PATH).getFile(),
                "-n", "myApp", "-p", "my.custom.package", "-k", "645", "-dev", "myDev",
                "-cat", "myCat", "-nd", "50", "-d", "2018-02-03 11:24:56.658974",
                "-r", "3.2", "-pr", "20", "-s", "56", "-vc", "3.6", "-vn", "myVn",
                "-sdk", "15", "-tsdk", "20", "-omp",
                getClass().getResource(APK).getFile()
        };
        PaprikaLauncher launcher = new PaprikaLauncher(args, silent);
        PaprikaApp app = launcher.analyzeApp(getClass().getResource(APK).getFile(), false, 0);
        assertThat(app.getName(), is(equalTo("myApp")));
        assertThat(app.getPackage(), is(equalTo("my.custom.package")));
        assertThat(app.getKey(), is(equalTo("645")));
        assertThat(app.getDeveloper(), is(equalTo("myDev")));
        assertThat(app.getCategory(), is(equalTo("myCat")));
        assertThat(app.getNbDownload(), is(equalTo(50)));
        assertThat(app.getDate(), is(equalTo("2018-02-03 11:24:56.658974")));
        assertThat(app.getRating(), is(closeTo(3.2, 0.1)));
        assertThat(app.getPrice(), is(equalTo("20")));
        assertThat(app.getSize(), is(equalTo(56)));
        assertThat(app.getVersionCode(), is(equalTo("3.6")));
        assertThat(app.getVersionName(), is(equalTo("myVn")));
        assertThat(app.getSdkVersion(), is(equalTo(15)));
        assertThat(app.getTargetSdkVersion(), is(equalTo(20)));
    }


}
