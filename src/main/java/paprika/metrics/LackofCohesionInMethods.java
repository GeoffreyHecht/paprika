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
 * Created by Geoffrey Hecht on 20/05/14.
 * LCOM2 (Chidamber et Kemerer 1991)
 */
public class LackofCohesionInMethods extends UnaryMetric<Integer> {

    private LackofCohesionInMethods(PaprikaClass paprikaClass) {
        this.value = paprikaClass.computeLCOM();
        this.entity = paprikaClass;
        this.name = "lack_of_cohesion_in_methods";
    }

    public static LackofCohesionInMethods createLackofCohesionInMethods(PaprikaClass paprikaClass) {
        LackofCohesionInMethods couplingBetweenObjects = new LackofCohesionInMethods(paprikaClass);
        couplingBetweenObjects.updateEntity();
        return couplingBetweenObjects;
    }
}
