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

package paprika.analyzer;

import paprika.entities.PaprikaArgument;
import paprika.entities.PaprikaMethod;
import paprika.entities.PaprikaVariable;
import paprika.metrics.methods.condition.IsGetterOrSetter;
import paprika.metrics.methods.condition.IsInit;
import paprika.metrics.methods.condition.IsOverride;
import paprika.metrics.methods.condition.MethodCondition;
import paprika.metrics.methods.stat.CyclomaticComplexity;
import paprika.metrics.methods.stat.MethodStatistic;
import paprika.metrics.methods.stat.NumberOfDeclaredLocals;
import paprika.metrics.methods.stat.NumberOfInstructions;
import soot.*;
import soot.grimp.GrimpBody;
import soot.jimple.FieldRef;

import java.util.Arrays;
import java.util.List;

public class BodyProcessor {

    private CyclomaticComplexity complexity = new CyclomaticComplexity();

    private List<MethodStatistic> statistics = Arrays.asList(
            new NumberOfDeclaredLocals(),
            new NumberOfInstructions(),
            complexity
    );

    private MethodCondition isInit = new IsInit();
    private MethodCondition isOverride = new IsOverride();
    private IsGetterOrSetter isGetterOrSetter = new IsGetterOrSetter();

    public void processMethodBody(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        registerArgs(sootMethod, paprikaMethod);
        GrimpBody activeBody = (GrimpBody) sootMethod.getActiveBody();
        statistics.forEach(stat -> stat.collectMetric(sootMethod, paprikaMethod));
        computeLackOfCohesion(sootMethod, paprikaMethod);

        if (!isInit.createIfMatching(sootMethod, paprikaMethod)) {
            isOverride.createIfMatching(sootMethod, paprikaMethod);
            isGetterOrSetter.createIfMatching(sootMethod, paprikaMethod, complexity.lastMethodHadASingleBranch());
        }
    }

    private void registerArgs(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        int i = 0;
        for (Type type : sootMethod.getParameterTypes()) {
            i++;
            PaprikaArgument.createPaprikaArgument(type.toString(), i, paprikaMethod);
        }
    }

    private void computeLackOfCohesion(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        for (Unit sootUnit : sootMethod.getActiveBody().getUnits()) {
            // LCOM
            List<ValueBox> boxes = sootUnit.getUseAndDefBoxes();
            for (ValueBox valueBox : boxes) {
                Value value = valueBox.getValue();
                if (value instanceof FieldRef) {
                    SootFieldRef field = ((FieldRef) value).getFieldRef();
                    if (field.declaringClass() == sootMethod.getDeclaringClass()) {
                        PaprikaVariable paprikaVariable = paprikaMethod.getPaprikaClass().findVariable(field.name());
                        // If we don't find the field it's inherited and thus not used for LCOM2
                        if (paprikaVariable != null) {
                            paprikaMethod.useVariable(paprikaVariable);
                        }
                    }
                }
            }
        }
    }

}
