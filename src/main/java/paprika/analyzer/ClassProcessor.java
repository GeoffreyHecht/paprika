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

package paprika.analyzer;

import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaVariable;
import paprika.metrics.app.NumberOfClasses;
import paprika.metrics.app.NumberOfVariables;
import paprika.metrics.classes.condition.CountedClassCondition;
import paprika.metrics.classes.condition.IsAbstractClass;
import paprika.metrics.classes.condition.IsInnerClassStatic;
import paprika.metrics.classes.condition.IsInterface;
import paprika.metrics.classes.condition.subclass.*;
import paprika.metrics.classes.stat.soot.DepthOfInheritance;
import paprika.metrics.classes.stat.soot.NumberOfAttributes;
import paprika.metrics.classes.stat.soot.NumberOfImplementedInterfaces;
import paprika.metrics.classes.stat.soot.SootClassStatistic;
import paprika.metrics.common.CommonCondition;
import paprika.metrics.common.IsFinal;
import paprika.metrics.common.IsStatic;
import paprika.metrics.common.NumberOfMethods;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.util.Chain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassProcessor {

    private List<CountedClassCondition> classConditions = Arrays.asList(
            new IsAbstractClass(),
            new IsInterface(),
            new IsInnerClassStatic()
    );

    private List<IsSubClass> subClassConditions = Arrays.asList(
            new IsActivity(),
            new IsApplication(),
            new IsAsyncTask(),
            new IsBroadcastReceiver(),
            new IsContentProvider(),
            new IsService(),
            new IsView()
    );

    private List<CommonCondition> conditions = Arrays.asList(
            new IsFinal(), // This must stay at index 0
            new IsStatic()
    );

    private List<SootClassStatistic> statistics = Arrays.asList(
            new DepthOfInheritance(),
            new NumberOfAttributes(),
            new NumberOfImplementedInterfaces(),
            new NumberOfMethods()
    );

    private PaprikaContainer container;
    private Map<SootClass, PaprikaClass> classMap;
    private String pack;
    private boolean mainPackageOnly;
    private int varCount = 0;

    public ClassProcessor(PaprikaContainer container, String pack, boolean mainPackageOnly) {
        this.container = container;
        this.classMap = container.getClassMap();
        this.pack = pack;
        this.mainPackageOnly = mainPackageOnly;
    }

    public void processClasses() {
        Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
        String rsubClassStart = pack + ".R$";
        String packs = pack.concat(".");
        String buildConfigClass = pack.concat(".BuildConfig");
        for (SootClass sootClass : sootClasses) {
            String name = sootClass.getName();
            if (name.startsWith(rsubClassStart) || name.equals(buildConfigClass)) {
                continue;
            }
            if (!mainPackageOnly || name.startsWith(packs)) {
                collectClassMetrics(sootClass);
            }
        }
        // Now that all classes have been processed at least once (and the map filled) we can process NOC
        for (SootClass sootClass : sootClasses) {
            if (sootClass.hasSuperclass()) {
                SootClass superClass = sootClass.getSuperclass();
                PaprikaClass paprikaClass = classMap.get(superClass);
                if (paprikaClass != null) classMap.get(superClass).addChildren();
            }
        }
        collectAppMetrics();
    }

    private void collectClassMetrics(SootClass sootClass) {
        PaprikaClass paprikaClass = container.addClass(sootClass);
        // Checking if the class is final
        conditions.get(0).createIfMatching(sootClass, paprikaClass);
        // Checking if the class is a child of a relevant subclass
        for (IsSubClass subClass : subClassConditions) {
            if (subClass.createIfMatching(sootClass, paprikaClass)) {
                break;
            }
        }
        classConditions.forEach(condition -> condition.createIfMatching(sootClass, paprikaClass));
        // Field analysis
        sootClass.getFields().forEach(field -> registerField(paprikaClass, field));
        // Numerical stats
        statistics.forEach(stat -> stat.collectMetric(sootClass, paprikaClass));
    }

    private void registerField(PaprikaClass paprikaClass, SootField sootField) {
        PaprikaVariable paprikaVariable = container.addField(paprikaClass, sootField);
        varCount++;
        conditions.forEach(condition -> condition.createIfMatching(sootField, paprikaVariable));
    }

    /**
     * Should be called after all classes have been processed once
     */
    private void collectAppMetrics() {
        NumberOfClasses.createNumberOfClasses(container.getPaprikaApp(), classMap.size());
        NumberOfVariables.createNumberOfVariables(container.getPaprikaApp(), varCount);
        classConditions.forEach(condition -> condition.createNumberMetric(container.getPaprikaApp()));
        subClassConditions.forEach(condition -> condition.createNumberMetric(container.getPaprikaApp()));
    }

}
