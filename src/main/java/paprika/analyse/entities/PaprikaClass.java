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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaClass extends Entity {

    // Neo4J attributes names
    public static final String N4J_APP_KEY = PaprikaApp.N4J_APP_KEY;
    public static final String N4J_NAME = "name";
    public static final String N4J_MODIFIER = "modifier";
    public static final String N4J_PARENT = "parent_name";

    private PaprikaClass parent;
    // Parent name to cover library case
    private String parentName;
    private int complexity;
    private int children;
    private Set<PaprikaClass> coupled;
    private Set<PaprikaMethod> paprikaMethods;
    private Set<PaprikaVariable> paprikaVariables;
    private Set<PaprikaClass> interfaces;
    private PaprikaModifier modifier;

    private PaprikaClass(String name, PaprikaModifier modifier) {
        this.setName(name);
        this.complexity = 0;
        this.children = 0;
        this.paprikaMethods = new HashSet<>();
        this.paprikaVariables = new HashSet<>();
        this.coupled = new HashSet<>();
        this.interfaces = new HashSet<>();
        this.modifier = modifier;
    }

    public static PaprikaClass create(String name, PaprikaApp paprikaApp, PaprikaModifier modifier) {
        PaprikaClass paprikaClass = new PaprikaClass(name, modifier);
        paprikaApp.addPaprikaClass(paprikaClass);
        return paprikaClass;
    }

    public Set<PaprikaVariable> getPaprikaVariables() {
        return paprikaVariables;
    }

    public Set<PaprikaMethod> getPaprikaMethods() {
        return paprikaMethods;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getModifierAsString() {
        return modifier.getName();
    }

    public PaprikaClass getParent() {
        return parent;
    }

    public Set<PaprikaClass> getInterfaces() {
        return interfaces;
    }

    public void setParent(PaprikaClass parent) {
        this.parent = parent;
    }

    public void addPaprikaMethod(PaprikaMethod paprikaMethod) {
        paprikaMethods.add(paprikaMethod);
    }

    public void addComplexity(int value) {
        complexity += value;
    }

    public void addChildren() {
        children += 1;
    }

    public int getComplexity() {
        return complexity;
    }

    public int getChildren() {
        return children;
    }

    public void coupledTo(PaprikaClass paprikaClass) {
        coupled.add(paprikaClass);
    }

    public void implement(PaprikaClass paprikaClass) {
        interfaces.add(paprikaClass);
    }

    public int getCouplingValue() {
        return coupled.size();
    }

    /**
     * Get the NPath complexity of the entire program
     * The NPath complexity is just the combinatorial of the cyclomatic complexity
     **/
    public double computeNPathComplexity() {
        return Math.pow(2.0, (double) getComplexity());
    }

    public void addPaprikaVariable(PaprikaVariable paprikaVariable) {
        paprikaVariables.add(paprikaVariable);
    }

    public PaprikaVariable findVariable(String name) {
        // First we are looking to the field declared by this class (any modifiers)
        for (PaprikaVariable paprikaVariable : paprikaVariables) {
            if (paprikaVariable.getName().equals(name)) return paprikaVariable;
        }
        // otherwise we return null
        return null;
    }
}
