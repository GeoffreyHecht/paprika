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

package paprika.analyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paprika.analyse.analyzer.ManifestProcessor;
import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.entities.PaprikaAppBuilder;
import paprika.analyse.metrics.Metric;
import paprika.analyse.metrics.app.IsDebuggableRelease;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ManifestProcessorTest {

    private static final String XML_FOLDER = "/manifests";

    private static final String XML_WITHOUT_DEBUG = XML_FOLDER + "/noDebugAttribute.xml";
    private static final String XML_DEBUG_FALSE = XML_FOLDER + "/debuggableFalse.xml";
    private static final String XML_DEBUG_TRUE = XML_FOLDER + "/debuggableTrue.xml";

    private PaprikaApp testApp;

    @BeforeEach
    public void setUp() {
        testApp = new PaprikaAppBuilder().create();
    }

    private void setupProcessor(String file) throws Exception {
        ManifestProcessor processor = new ManifestProcessor(testApp, "debug_apk_path");
        processor.parseManifestText(new String(
                Files.readAllBytes(Paths.get(getClass().getResource(file).toURI()))));
    }

    @Test
    public void notDebuggable() throws Exception {
        setupProcessor(XML_WITHOUT_DEBUG);
        for (Metric metric : testApp.getMetrics()) {
            assertThat(metric.getClass(), is(not(IsDebuggableRelease.class)));
        }
    }

    @Test
    public void debuggableFalse() throws Exception {
        setupProcessor(XML_DEBUG_FALSE);
        for (Metric metric : testApp.getMetrics()) {
            assertThat(metric.getClass(), is(not(IsDebuggableRelease.class)));
        }
    }

    @Test
    public void debuggableTrue() throws Exception {
        setupProcessor(XML_DEBUG_TRUE);
        assertThat(testApp.getMetrics().size(), is(greaterThanOrEqualTo(1)));
        assertThat(testApp.getMetrics(), hasItem(instanceOf(IsDebuggableRelease.class)));
    }

}
