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

package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Antonin Carette on 16-04-29.
 */
public class NPathComplexity extends UnaryMetric<Integer> {

    private NPathComplexity(PaprikaClass paprikaClass) {
        this.value = paprikaClass.computeNPathComplexity();
        this.entity = paprikaClass;
        this.name = "npath_complexity";
    }

    public static NPathComplexity createNPathComplexity(PaprikaClass paprikaClass) {
        NPathComplexity npath_complexity = new NPathComplexity(paprikaClass);
        npath_complexity.updateEntity();
        return npath_complexity;
    }

}
