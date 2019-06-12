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

package paprika.analyse.metrics.classes.condition.counted;

import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.entities.PaprikaClass;
import paprika.analyse.metrics.UnaryMetric;
import paprika.analyse.metrics.classes.condition.ClassCondition;
import soot.SootClass;

/**
 * A class condition that also includes a metric on the Paprika application to count the number
 * of times it was created.
 */
public abstract class CountedClassCondition extends ClassCondition {

    private String numberMetric;
    private int count = 0;

    /**
     * Constructor.
     *
     * @param conditionMetric the name of the metric created on a PaprikaClass
     * @param numberMetric    the name of the count metric created on a PaprikaApp
     */
    public CountedClassCondition(String conditionMetric, String numberMetric) {
        super(conditionMetric);
        this.numberMetric = numberMetric;
    }

    @Override
    public boolean createIfMatching(SootClass item, PaprikaClass paprikaClass) {
        if (super.createIfMatching(item, paprikaClass)) {
            count++;
            return true;
        }
        return false;
    }

    /**
     * Creates the count metric on the given application.
     *
     * @param app the app to bind the count metric to
     */
    public void createNumberMetric(PaprikaApp app) {
        UnaryMetric<Integer> metric = new UnaryMetric<>(numberMetric, app, count);
        metric.updateEntity();
    }

}
