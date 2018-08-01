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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaExternalMethod extends Entity {

    // Neo4J attributes names
    public static final String APP_KEY = PaprikaApp.APP_KEY;
    public static final String NAME = "name";
    public static final String FULL_NAME = "full_name";
    public static final String RETURN_TYPE = "return_type";

    private PaprikaExternalClass paprikaExternalClass;
    private List<PaprikaExternalArgument> paprikaExternalArguments;
    private String returnType;

    public String getReturnType() {
        return returnType;
    }

    public List<PaprikaExternalArgument> getPaprikaExternalArguments() {
        return paprikaExternalArguments;
    }

    private PaprikaExternalMethod(String name, String returnType,
                                  PaprikaExternalClass paprikaExternalClass) {
        this.setName(name);
        this.paprikaExternalClass = paprikaExternalClass;
        this.returnType = returnType;
        this.paprikaExternalArguments = new ArrayList<>();
    }

    public static PaprikaExternalMethod create(String name, String returnType, PaprikaExternalClass paprikaClass) {
        PaprikaExternalMethod paprikaMethod = new PaprikaExternalMethod(name, returnType, paprikaClass);
        paprikaClass.addPaprikaExternalMethod(paprikaMethod);
        return paprikaMethod;
    }

    public PaprikaExternalClass getPaprikaExternalClass() {
        return paprikaExternalClass;
    }

    public void setPaprikaExternalClass(PaprikaExternalClass paprikaClass) {
        this.paprikaExternalClass = paprikaClass;
    }

    @Override
    public String toString() {
        return this.getName() + "#" + paprikaExternalClass;
    }

    public void addExternalArgument(PaprikaExternalArgument paprikaExternalArgument) {
        this.paprikaExternalArguments.add(paprikaExternalArgument);
    }
}
