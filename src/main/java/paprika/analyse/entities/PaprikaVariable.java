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

/**
 * Created by Geoffrey Hecht on 26/06/14.
 */
public class PaprikaVariable extends Entity {

    private String type;
    private PaprikaModifier modifier;

    // Neo4J attributes names
    public static final String N4J_APP_KEY = PaprikaApp.N4J_APP_KEY;
    public static final String N4J_NAME = "name";
    public static final String N4J_MODIFIER = "modifier";
    public static final String N4J_TYPE = "type";

    public String getType() {
        return type;
    }

    private PaprikaVariable(String name, String type, PaprikaModifier modifier) {
        this.type = type;
        this.name = name;
        this.modifier = modifier;
    }

    public static PaprikaVariable create(String name, String type, PaprikaModifier modifier,
                                         PaprikaClass paprikaClass) {
        PaprikaVariable paprikaVariable = new PaprikaVariable(name, type, modifier);
        paprikaClass.addPaprikaVariable(paprikaVariable);
        return paprikaVariable;
    }

    public String getModifierAsString() {
        return modifier.getName();
    }

    public boolean isPublic() {
        return modifier == PaprikaModifier.PUBLIC;
    }

    public boolean isPrivate() {
        return modifier == PaprikaModifier.PRIVATE;
    }

    public boolean isProtected() {
        return modifier == PaprikaModifier.PROTECTED;
    }
}
