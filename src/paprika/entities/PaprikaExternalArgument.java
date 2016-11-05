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

package paprika.entities;

/**
 * Created by Geoffrey Hecht on 13/08/14.
 */
public class PaprikaExternalArgument extends Entity{
    private PaprikaExternalMethod paprikaExternalMethod;
    private int position;

    private PaprikaExternalArgument(String name, int position, PaprikaExternalMethod paprikaExternalMethod) {
        this.paprikaExternalMethod = paprikaExternalMethod;
        this.name = name;
        this.position = position;
    }

    public static PaprikaExternalArgument createPaprikaExternalArgument(String name, int position,PaprikaExternalMethod paprikaExternalMethod){
        PaprikaExternalArgument paprikaExternalArgument = new PaprikaExternalArgument(name,position,paprikaExternalMethod);
        paprikaExternalMethod.addExternalArgument(paprikaExternalArgument);
        return paprikaExternalArgument;
    }

    public int getPosition() {
        return position;
    }
}
