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

import soot.SootMethod;

import java.util.regex.Pattern;

public class UsesPublicData extends MethodCondition {

    public static final String NAME = "uses_public_data";

    private static final String REGEX =
            "openFileOutput\\(java\\.lang\\.String,int\\)>\\(\"[^\"]*\", [12]\\)";

    private Pattern pattern;

    public UsesPublicData() {
        super(NAME);
        this.pattern = Pattern.compile(REGEX);
    }

    @Override
    public boolean matches(SootMethod item) {
        String body = item.getActiveBody().toString();
        if (body.contains("openFileOutput")) {
            return pattern.matcher(body).find();
        }
        return false;
    }

}
