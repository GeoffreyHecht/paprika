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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaExternalClass extends Entity{
    private PaprikaApp paprikaApp;
    private String parentName;
    private Set<PaprikaExternalMethod> paprikaExternalMethods;

    public Set<PaprikaExternalMethod> getPaprikaExternalMethods() {
        return paprikaExternalMethods;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    private PaprikaExternalClass(String name, PaprikaApp paprikaApp) {
        this.setName(name);
        this.paprikaApp = paprikaApp;
        this.paprikaExternalMethods  = new HashSet<>();
    }

    public static PaprikaExternalClass createPaprikaExternalClass(String name, PaprikaApp paprikaApp) {
        PaprikaExternalClass paprikaClass = new PaprikaExternalClass(name, paprikaApp);
        paprikaApp.addPaprikaExternalClass(paprikaClass);
        return paprikaClass;
    }

    public void addPaprikaExternalMethod(PaprikaExternalMethod paprikaMethod){
        paprikaExternalMethods.add(paprikaMethod);
    }

    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    public void setPaprikaApp(PaprikaApp paprikaApp) {
        this.paprikaApp = paprikaApp;
    }

}
