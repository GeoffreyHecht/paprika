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

package paprika.analyse.analyzer;

import net.dongliu.apk.parser.ApkFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.metrics.app.IsDebuggableRelease;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ManifestProcessor {

    private static final String APP_NODE = "/manifest/application";
    private static final String DEBUG_ATTRIBUTE = "android:debuggable";

    private String apkPath;
    private PaprikaApp app;

    public ManifestProcessor(PaprikaApp app, String apkPath) {
        this.apkPath = apkPath;
        this.app = app;
    }

    public void parseManifest() throws ManifestException {
        try (ApkFile apkFile = new ApkFile(apkPath)) {
            parseManifestText(apkFile.getManifestXml());
        } catch (IOException e) {
            throw new ManifestException(apkPath, e);
        }
    }

    public void parseManifestText(String text) throws ManifestException {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document root = builder.parse(new ByteArrayInputStream(text.getBytes()));
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node appNode = (Node) xPath.compile(APP_NODE).evaluate(root, XPathConstants.NODE);
            Node attribute = appNode.getAttributes().getNamedItem(DEBUG_ATTRIBUTE);
            if (attribute != null && Boolean.valueOf(attribute.getNodeValue())) {
                IsDebuggableRelease.createMetric(app);
            }
        } catch (IOException | ParserConfigurationException
                | SAXException | XPathExpressionException e) {
            throw new ManifestException(apkPath, e);
        }
    }

}
