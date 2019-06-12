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

package paprika.analyse.metrics.classes.condition.counted.subclass;

import paprika.analyse.metrics.classes.condition.counted.CountedClassCondition;
import soot.SootClass;

/**
 * Metric used to count subclasses of specific Android classes: Activities, Views...
 */
public abstract class IsSubClass extends CountedClassCondition {

    private String androidClass;

    /**
     * Constructor.
     *
     * @param conditionMetric the name of the metric created on a PaprikaClass
     * @param numberMetric    the name of the count metric created on a PaprikaApp
     * @param androidClass    the name of the Android class to check
     */
    public IsSubClass(String conditionMetric, String numberMetric, String androidClass) {
        super(conditionMetric, numberMetric);
        this.androidClass = androidClass;
    }

    @Override
    public boolean matches(SootClass item) {
        return isSubClass(item, androidClass);
    }

    protected boolean isSubClass(SootClass sootClass, String className) {
        do {
            if (sootClass.getName().equals(className)) return true;
            sootClass = sootClass.getSuperclass();
        } while (sootClass.hasSuperclass());
        return false;
    }

}
