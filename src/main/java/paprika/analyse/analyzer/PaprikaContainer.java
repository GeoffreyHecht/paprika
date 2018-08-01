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

package paprika.analyse.analyzer;

import paprika.analyse.entities.*;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

public class PaprikaContainer {

    private Map<SootClass, PaprikaClass> classMap;
    private Map<SootMethod, PaprikaMethod> methodMap;
    private Map<SootClass, PaprikaExternalClass> externalClassMap;
    private Map<SootMethod, PaprikaExternalMethod> externalMethodMap;

    private PaprikaApp paprikaApp;

    public PaprikaContainer(PaprikaApp app) {
        this.paprikaApp = app;
        this.classMap = new HashMap<>();
        this.methodMap = new HashMap<>();
        this.externalClassMap = new HashMap<>();
        this.externalMethodMap = new HashMap<>();
    }

    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    public Map<SootClass, PaprikaClass> getClassMap() {
        return classMap;
    }

    public Map<SootMethod, PaprikaMethod> getMethodMap() {
        return methodMap;
    }

    public Map<SootMethod, PaprikaExternalMethod> getExternalMethodMap() {
        return externalMethodMap;
    }

    public PaprikaClass addClass(SootClass sootClass) {
        PaprikaClass paprikaClass = PaprikaClass.create(sootClass.getName(),
                paprikaApp, PaprikaModifier.getModifier(sootClass));
        if (sootClass.hasSuperclass()) {
            paprikaClass.setParentName(sootClass.getSuperclass().getName());
        }
        this.classMap.put(sootClass, paprikaClass);
        return paprikaClass;
    }

    public PaprikaVariable addField(PaprikaClass paprikaClass, SootField sootField) {
        return PaprikaVariable.create(
                sootField.getName(), sootField.getType().toString(), PaprikaModifier.getModifier(sootField),
                paprikaClass);
    }

    public void addMethod(SootMethod sootMethod) {
        SootClass sootClass = sootMethod.getDeclaringClass();
        PaprikaClass paprikaClass = classMap.get(sootClass);
        if (paprikaClass == null) {
            // Should be R or external classes
            try {
                sootClass.setLibraryClass();
            } catch (NullPointerException e) {
                // Soot issue. Can be safely ignored.
                return;
            }
            return;
        }
        PaprikaMethod paprikaMethod = PaprikaMethod.create(sootMethod.getName(),
                PaprikaModifier.getModifier(sootMethod),
                sootMethod.getReturnType().toString(), paprikaClass);
        methodMap.put(sootMethod, paprikaMethod);
    }

    public PaprikaExternalClass getOrCreateExternalClass(SootClass sootClass) {
        PaprikaExternalClass paprikaExternalClass = externalClassMap.get(sootClass);
        if (paprikaExternalClass == null) {
            paprikaExternalClass = PaprikaExternalClass.create(sootClass.getName(), paprikaApp);
            externalClassMap.put(sootClass, paprikaExternalClass);
        }
        return paprikaExternalClass;
    }

    public void computeInheritance() {
        for (Map.Entry entry : classMap.entrySet()) {
            SootClass sClass = (SootClass) entry.getKey();
            PaprikaClass pClass = (PaprikaClass) entry.getValue();
            SootClass sParent = sClass.getSuperclass();
            PaprikaClass pParent = classMap.get(sParent);
            if (pParent != null) {
                pClass.setParent(pParent);
            }
        }
    }

    public void computeInterface() {
        for (Map.Entry entry : classMap.entrySet()) {
            SootClass sClass = (SootClass) entry.getKey();
            PaprikaClass pClass = (PaprikaClass) entry.getValue();
            for (SootClass SInterface : sClass.getInterfaces()) {
                PaprikaClass pInterface = classMap.get(SInterface);
                if (pInterface != null) {
                    pClass.implement(pInterface);
                }
            }
        }
    }
}
