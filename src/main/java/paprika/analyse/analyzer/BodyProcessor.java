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

import paprika.analyse.entities.PaprikaArgument;
import paprika.analyse.entities.PaprikaMethod;
import paprika.analyse.entities.PaprikaVariable;
import paprika.analyse.metrics.methods.condition.*;
import paprika.analyse.metrics.methods.stat.CyclomaticComplexity;
import paprika.analyse.metrics.methods.stat.MethodStatistic;
import paprika.analyse.metrics.methods.stat.NumberOfDeclaredLocals;
import paprika.analyse.metrics.methods.stat.NumberOfInstructions;
import soot.*;
import soot.grimp.GrimpBody;
import soot.jimple.FieldRef;

import java.util.List;

public class BodyProcessor {

    private CyclomaticComplexity complexity = new CyclomaticComplexity();

    private MethodStatistic[] statistics = {
            new NumberOfDeclaredLocals(),
            new NumberOfInstructions(),
            complexity
    };

    private MethodCondition isInit;
    private MethodCondition isOverride;
    private IsGetterOrSetter isGetterOrSetter;
    private MethodCondition usesPublicData;

    public BodyProcessor() {
        this.usesPublicData = new UsesPublicData();
        this.isInit = new IsInit();
        this.isGetterOrSetter = new IsGetterOrSetter();
        this.isOverride = new IsOverride();
    }

    public void processMethodBody(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        registerArgs(sootMethod, paprikaMethod);
        GrimpBody activeBody = (GrimpBody) sootMethod.getActiveBody();
        for (MethodStatistic stat : statistics) {
            stat.collectMetric(sootMethod, paprikaMethod);
        }
        computeLackOfCohesion(sootMethod, paprikaMethod);

        if (!isInit.createIfMatching(sootMethod, paprikaMethod)) {
            isOverride.createIfMatching(sootMethod, paprikaMethod);
            isGetterOrSetter.createIfMatching(sootMethod, paprikaMethod, complexity.lastMethodHadASingleBranch());
        }
        usesPublicData.createIfMatching(sootMethod, paprikaMethod);
    }

    private void registerArgs(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        int i = 0;
        for (Type type : sootMethod.getParameterTypes()) {
            i++;
            PaprikaArgument.create(type.toString(), i, paprikaMethod);
        }
    }

    private void computeLackOfCohesion(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        for (Unit sootUnit : sootMethod.getActiveBody().getUnits()) {
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
