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

import paprika.entities.PaprikaClass;
import paprika.metrics.common.IsStatic;
import soot.SootClass;
import soot.SootField;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */
public class IsInnerClassStatic extends CountedClassCondition {

    public IsInnerClassStatic() {
        super("is_inner_class", "number_of_inner_classes");
    }

    @Override
    public boolean matches(SootClass sootClass) {
        return sootClass.isInnerClass();
    }

    @Override
    public boolean createIfMatching(SootClass sootClass, PaprikaClass paprikaClass) {
        boolean match = super.createIfMatching(sootClass, paprikaClass);
        if (match && sootClass != null && isInnerClassStatic(sootClass)) {
            IsStatic.createIsStatic(paprikaClass);
        }
        return match;
    }

    /**
     * Fix to determine if a class is static or not
     */
    private boolean isInnerClassStatic(SootClass innerClass) {
        for (SootField sootField : innerClass.getFields()) {
            // we are looking if the field for non static inner class generated during the compilation (with the convention name) exists
            if (sootField.getName().equals("this$0")) {
                // in this case we can return false
                return false;
            }
        }
        return true;
    }

}
