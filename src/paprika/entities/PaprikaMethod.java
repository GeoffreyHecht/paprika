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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaMethod extends Entity{
    private PaprikaClass paprikaClass;
    private String returnType;
    private Set<PaprikaVariable> usedVariables;
    private Set<Entity> calledMethods;
    private PaprikaModifiers modifier;
    private List<PaprikaArgument> arguments;
    public String getReturnType() {
        return returnType;
    }

    public PaprikaModifiers getModifier() {
        return modifier;
    }

    private PaprikaMethod(String name, PaprikaModifiers modifier, String returnType, PaprikaClass paprikaClass) {
        this.setName(name);
        this.paprikaClass = paprikaClass;
        this.usedVariables = new HashSet<>(0);
        this.calledMethods = new HashSet<>(0);
        this.arguments = new ArrayList<>(0);
        this.modifier = modifier;
        this.returnType = returnType;
    }

    public static PaprikaMethod createPaprikaMethod(String name, PaprikaModifiers modifier, String returnType,  PaprikaClass paprikaClass) {
        PaprikaMethod paprikaMethod = new PaprikaMethod(name, modifier, returnType, paprikaClass);
        paprikaClass.addPaprikaMethod(paprikaMethod);
        return  paprikaMethod;
    }

    public PaprikaClass getPaprikaClass() {
        return paprikaClass;
    }

    public void setPaprikaClass(PaprikaClass paprikaClass) {
        this.paprikaClass = paprikaClass;
    }

    @Override
    public String toString() {
        return this.getName() + "#" + paprikaClass;
    }

    public void useVariable(PaprikaVariable paprikaVariable) {
        usedVariables.add(paprikaVariable);
    }

    public Set<PaprikaVariable> getUsedVariables(){
        return this.usedVariables;
    }

    public void callMethod(Entity paprikaMethod) { calledMethods.add(paprikaMethod);}

    public Set<Entity> getCalledMethods() { return this.calledMethods; }

    public boolean haveCommonFields(PaprikaMethod paprikaMethod){
        Set<PaprikaVariable> otherVariables = paprikaMethod.getUsedVariables();
        for(PaprikaVariable paprikaVariable : usedVariables){
            if(otherVariables.contains(paprikaVariable)) return true;
        }
        return false;
    }

    public void addArgument(PaprikaArgument paprikaArgument){
        this.arguments.add(paprikaArgument);
    }

    public List<PaprikaArgument> getArguments(){
        return arguments;
    }
}
