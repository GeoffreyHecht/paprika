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

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */
public class IsFinal extends CommonCondition{

    public IsFinal() {
        super("is_final");
    }

    @Override
    public boolean matches(SootClass sootClass) {
        return sootClass.isFinal();
    }

    @Override
    public boolean matches(SootField sootField) {
        return sootField.isFinal();
    }

    @Override
    public boolean matches(SootMethod sootMethod) {
        return sootMethod.isFinal();
    }

}
