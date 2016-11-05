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

import paprika.entities.PaprikaExternalArgument;

/**
 * Created by antonin on 16-04-29.
 */
public class IsARGB8888 extends UnaryMetric<Boolean> {

    private IsARGB8888(PaprikaExternalArgument paprikaExternalArgument, boolean value) {
        this.value = value;
        this.entity = paprikaExternalArgument;
        this.name = "is_argb_8888";
    }

    public static IsARGB8888 createIsARGB8888(PaprikaExternalArgument paprikaExternalArgument, boolean value) {
        IsARGB8888 isARGB8888 = new IsARGB8888(paprikaExternalArgument, value);
        isARGB8888.updateEntity();
        return isARGB8888;
    }

}
