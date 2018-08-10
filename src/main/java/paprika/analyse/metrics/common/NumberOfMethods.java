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

package paprika.analyse.metrics.common;

import paprika.analyse.entities.Entity;
import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.entities.PaprikaClass;
import paprika.analyse.metrics.UnaryMetric;
import paprika.analyse.metrics.classes.stat.soot.SootClassStatistic;
import soot.SootClass;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 * <p>
 * Can be applied to a class or an application.
 */
public class NumberOfMethods implements SootClassStatistic {

    public static final String NAME = "number_of_methods";

    public static void createNumberOfMethods(PaprikaApp app, int value) {
        createMetric(app, value);
    }

    private static void createMetric(Entity entity, int value) {
        UnaryMetric<Integer> metric = new UnaryMetric<>(NAME, entity, value);
        metric.updateEntity();
    }

    @Override
    public void collectMetric(SootClass sootClass, PaprikaClass paprikaClass) {
        createMetric(paprikaClass, sootClass.getMethodCount());
    }

}
