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

package paprika.metrics.methods.condition;

import paprika.entities.PaprikaMethod;
import paprika.entities.PaprikaVariable;
import paprika.metrics.UnaryMetric;
import soot.SootMethod;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */
public class IsGetterOrSetter {

    public static final String IS_GETTER_NAME = "is_getter";
    public static final String IS_SETTER_NAME = "is_setter";

    public void createIfMatching(SootMethod sootMethod, PaprikaMethod paprikaMethod,
                                 boolean lastMethodHadSingleBranch) {
        if (lastMethodHadSingleBranch && paprikaMethod.getUsedVariables().size() == 1
                && sootMethod.getExceptions().size() == 0) {
            PaprikaVariable paprikaVariable = paprikaMethod.getUsedVariables().iterator().next();
            int parameterCount = sootMethod.getParameterCount();
            int unitSize = sootMethod.getActiveBody().getUnits().size();
            String returnType = paprikaMethod.getReturnType();
            if (parameterCount == 1 && unitSize <= 4 && returnType.equals("void")) {
                createMetric(IS_SETTER_NAME, paprikaMethod);
            } else if (parameterCount == 0 && unitSize <= 3 && returnType.equals(paprikaVariable.getType())) {
                createMetric(IS_GETTER_NAME, paprikaMethod);
            }
        }
    }

    private void createMetric(String name, PaprikaMethod paprikaMethod) {
        UnaryMetric<Boolean> metric = new UnaryMetric<>(name, paprikaMethod, true);
        metric.updateEntity();
    }


}
