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

package paprika.analyse.entities;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * Created by Geoffrey Hecht on 08/07/14.
 */
public enum PaprikaModifier {

    PRIVATE("private"),
    PROTECTED("protected"),
    PUBLIC("public");

    private String name;

    PaprikaModifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PaprikaModifier getModifier(SootClass sootClass) {
        return chooseModifier(sootClass.isPublic(), sootClass.isProtected());
    }

    public static PaprikaModifier getModifier(SootField sootField) {
        return chooseModifier(sootField.isPublic(), sootField.isProtected());
    }

    public static PaprikaModifier getModifier(SootMethod sootMethod) {
        return chooseModifier(sootMethod.isPublic(), sootMethod.isProtected());
    }

    private static PaprikaModifier chooseModifier(boolean isPublic, boolean isProtected) {
        if (isPublic) return PUBLIC;
        if (isProtected) return PROTECTED;
        return PRIVATE;
    }
}
