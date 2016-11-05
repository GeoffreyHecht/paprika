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

package paprika.entities;

/**
 * Created by Geoffrey Hecht on 26/06/14.
 */
public class PaprikaVariable extends Entity{
    private PaprikaClass paprikaClass;
    private String type;
    private PaprikaModifiers modifier;

    public String getType() {
        return type;
    }

    public PaprikaModifiers getModifier() {
        return modifier;
    }

    private PaprikaVariable(String name, String type, PaprikaModifiers modifier, PaprikaClass paprikaClass) {
        this.type = type;
        this.name = name;
        this.modifier = modifier;
        this.paprikaClass = paprikaClass;
    }

    public static PaprikaVariable createPaprikaVariable(String name, String type, PaprikaModifiers modifier, PaprikaClass paprikaClass) {
        PaprikaVariable paprikaVariable = new PaprikaVariable(name, type, modifier, paprikaClass);
        paprikaClass.addPaprikaVariable(paprikaVariable);
        return paprikaVariable;
    }

    public boolean isPublic(){
        return modifier == PaprikaModifiers.PUBLIC;
    }

    public boolean isPrivate(){
        return modifier == PaprikaModifiers.PRIVATE;
    }

    public boolean isProtected(){ return modifier == PaprikaModifiers.PROTECTED; }
}
