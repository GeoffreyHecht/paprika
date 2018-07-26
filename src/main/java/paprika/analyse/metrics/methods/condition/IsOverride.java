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

package paprika.analyse.metrics.methods.condition;

import soot.SootClass;
import soot.SootMethod;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */
public class IsOverride extends MethodCondition {

    public static final String NAME = "is_override";

    public IsOverride() {
        super(NAME);
    }

    @Override
    public boolean matches(SootMethod sootMethod) {
        SootClass sootClass = sootMethod.getDeclaringClass();
        for (SootClass inter : sootClass.getInterfaces()) {
            if (classContainsMethod(inter, sootMethod)) return true;
            while (inter.hasSuperclass()) {
                inter = inter.getSuperclass();
                if (classContainsMethod(inter, sootMethod)) return true;
            }
        }
        while (sootClass.hasSuperclass()) {
            sootClass = sootClass.getSuperclass();
            if (classContainsMethod(sootClass, sootMethod)) return true;
        }
        return false;
    }


    /**
     * Test if a class contains a method with same name, parameters and return type
     */
    private boolean classContainsMethod(SootClass sootClass, SootMethod sootMethod) {
        // Here unsafe just means it will return null (instead of throwing an exception)
        return sootClass.getMethodUnsafe(sootMethod.getName(),
                sootMethod.getParameterTypes(), sootMethod.getReturnType()) != null;
    }

}
