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

package paprika.neo4jBolt;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public enum RelationTypes implements RelationshipType {
    APP_OWNS_CLASS,
    CLASS_OWNS_METHOD,
    CLASS_OWNS_VARIABLE,
    METHOD_OWNS_ARGUMENT,
    IMPLEMENTS,
    EXTENDS,
    CALLS,
    USES,
    APP_USES_LIBRARY
}
