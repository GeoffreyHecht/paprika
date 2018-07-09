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

package paprika.metrics.common;

import paprika.entities.Entity;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.entities.PaprikaVariable;
import paprika.metrics.UnaryMetric;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

@SuppressWarnings("UnusedReturnValue")
public abstract class CommonCondition {

    private String metricName;

    public CommonCondition(String metricName) {
        this.metricName = metricName;
    }

    public boolean matches(SootClass sootClass) {
        return false;
    }

    public boolean matches(SootField sootField) {
        return false;
    }

    public boolean matches(SootMethod sootMethod) {
        return false;
    }

    public boolean createIfMatching(SootClass sootClass, PaprikaClass paprikaClass) {
        return createMetric(matches(sootClass), paprikaClass);
    }

    public boolean createIfMatching(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        return createMetric(matches(sootMethod), paprikaMethod);
    }

    public boolean createIfMatching(SootField sootField, PaprikaVariable paprikaVariable) {
        return createMetric(matches(sootField), paprikaVariable);
    }

    public boolean createMetric(boolean match, Entity entity) {
        if (match) {
            UnaryMetric<Boolean> metric = new UnaryMetric<>(metricName, entity, true);
            metric.updateEntity();
            return true;
        }
        return false;
    }

}
