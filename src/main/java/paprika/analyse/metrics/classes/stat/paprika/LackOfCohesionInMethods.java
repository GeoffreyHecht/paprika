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

package paprika.analyse.metrics.classes.stat.paprika;

import paprika.analyse.entities.PaprikaClass;
import paprika.analyse.entities.PaprikaMethod;
import paprika.analyse.metrics.UnaryMetric;

import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 * LCOM2 (Chidamber et Kemerer 1991)
 */
public class LackOfCohesionInMethods implements PaprikaClassStatistic {

    public static final String NAME = "lack_of_cohesion_in_methods";

    @Override
    public void collectMetric(PaprikaClass paprikaClass) {
        UnaryMetric<Integer> metric = new UnaryMetric<>(NAME, paprikaClass,
                computeLCOM(paprikaClass));
        metric.updateEntity();
    }

    private int computeLCOM(PaprikaClass paprikaClass) {
        Set<PaprikaMethod> methodSet = paprikaClass.getPaprikaMethods();
        PaprikaMethod[] methods = methodSet.toArray(new PaprikaMethod[0]);
        int methodCount = methods.length;
        int haveFieldInCommon = 0;
        int noFieldInCommon = 0;
        for (int i = 0; i < methodCount; i++) {
            for (int j = i + 1; j < methodCount; j++) {
                if (methods[i].haveCommonFields(methods[j])) {
                    haveFieldInCommon++;
                } else {
                    noFieldInCommon++;
                }
            }
        }
        int lcom = noFieldInCommon - haveFieldInCommon;
        return lcom > 0 ? lcom : 0;
    }

}
