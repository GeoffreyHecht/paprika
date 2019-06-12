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

package paprika.analyse.metrics.classes.stat.soot;

import paprika.analyse.entities.PaprikaClass;
import soot.SootClass;

/**
 * A statistic on a class that needs both the Soot and paprika application models
 * to be processed.
 */
public interface SootClassStatistic {

    /**
     * Collects and creates the metric on a given class.
     *
     * @param sootClass    the Soot representation of the class
     * @param paprikaClass the Paprika representation of the class
     */
    void collectMetric(SootClass sootClass, PaprikaClass paprikaClass);

}
