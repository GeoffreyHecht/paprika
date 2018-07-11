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

package paprika.metrics.methods.stat;

import paprika.entities.PaprikaMethod;
import paprika.metrics.UnaryMetric;
import soot.SootMethod;
import soot.Unit;
import soot.grimp.internal.GLookupSwitchStmt;

/**
 * Created by Geoffrey Hecht on 22/05/14.
 */
public class CyclomaticComplexity implements MethodStatistic {

    public static final String NAME = "cyclomatic_complexity";

    private int lastMethodBranches;

    @Override
    public void collectMetric(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        int complexity = getCyclomaticComplexity(sootMethod);
        UnaryMetric<Integer> metric = new UnaryMetric<>(NAME, paprikaMethod,
                complexity);
        metric.updateEntity();
        paprikaMethod.getPaprikaClass().addComplexity(complexity);
    }

    private int getCyclomaticComplexity(SootMethod sootMethod) {
        int nbOfBranches = 1;
        for (Unit sootUnit :sootMethod.getActiveBody().getUnits()) {
            // Cyclomatic complexity
            if (sootUnit.branches()) {
                if (sootUnit.fallsThrough()) nbOfBranches++;
                else if (sootUnit instanceof GLookupSwitchStmt)
                    nbOfBranches += ((GLookupSwitchStmt) sootUnit).getLookupValues().size();
            }
        }
        this.lastMethodBranches = nbOfBranches;
        return nbOfBranches;
    }

    public boolean lastMethodHadASingleBranch() {
        return lastMethodBranches == 1;
    }

}
