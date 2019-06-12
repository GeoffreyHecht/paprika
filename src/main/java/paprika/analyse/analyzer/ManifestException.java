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

/**
 * Exception thrown when failing to parse the manifest of an application.
 */
public class ManifestException extends AnalyzerException {

    /**
     * Constructor.
     *
     * @param apk   the apk that could not be analyzed
     * @param cause the underlying cause of the exception
     */
    public ManifestException(String apk, Throwable cause) {
        super("The manifest from " + apk + " could not be parsed.", cause);
    }

}
