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

package paprika.metrics.classes.condition;

import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.metrics.Condition;
import paprika.metrics.UnaryMetric;
import soot.SootClass;

public abstract class CountedClassCondition extends Condition<SootClass, PaprikaClass> {

    private String numberMetric;
    private int count = 0;

    public CountedClassCondition(String conditionMetric, String numberMetric) {
        super(conditionMetric);
        this.numberMetric = numberMetric;
    }

    @Override
    public boolean createIfMatching(SootClass sootClass, PaprikaClass paprikaClass) {
        if (super.createIfMatching(sootClass, paprikaClass)) {
            count++;
            return true;
        }
        return false;
    }

    public void createNumberMetric(PaprikaApp app) {
        UnaryMetric<Integer> metric = new UnaryMetric<>(numberMetric, app, count);
        metric.updateEntity();
    }

}
