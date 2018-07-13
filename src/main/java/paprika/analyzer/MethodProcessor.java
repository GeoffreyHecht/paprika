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

import paprika.entities.*;
import paprika.metrics.common.*;
import paprika.metrics.methods.NumberOfCallers;
import paprika.metrics.methods.NumberOfDirectCalls;
import paprika.metrics.methods.condition.IsSynchronized;
import paprika.metrics.methods.stat.NumberOfParameters;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MethodProcessor {

    private PaprikaContainer container;
    private Map<SootMethod, PaprikaMethod> methodMap;
    private Map<SootMethod, PaprikaExternalMethod> externalMethodMap;
    private BodyProcessor bodyProcessor;

    private List<CommonCondition> commonConditions = Arrays.asList(
            new IsStatic(),
            new IsFinal(),
            new IsAbstract()
    );

    private IsSynchronized isSynchronized = new IsSynchronized();
    private NumberOfParameters parameters = new NumberOfParameters();


    public MethodProcessor(PaprikaContainer container) {
        this.container = container;
        this.methodMap = container.getMethodMap();
        this.externalMethodMap = container.getExternalMethodMap();
        bodyProcessor = new BodyProcessor();
    }

    public void processMethods() {
        CallGraph callGraph = Scene.v().getCallGraph();
        for (Map.Entry<SootMethod, PaprikaMethod> entry : methodMap.entrySet()) {
            collectStandardMetrics(entry.getKey(), entry.getValue());
            collectCallGraphMetrics(callGraph, entry.getKey(), entry.getValue());
        }
        NumberOfMethods.createNumberOfMethods(container.getPaprikaApp(), methodMap.size());
    }

    private void collectStandardMetrics(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        commonConditions.forEach(condition -> condition.createIfMatching(sootMethod, paprikaMethod));
        isSynchronized.createIfMatching(sootMethod, paprikaMethod);
        parameters.collectMetric(sootMethod, paprikaMethod);
        if (sootMethod.hasActiveBody()) {
            bodyProcessor.processMethodBody(sootMethod, paprikaMethod);
        }
    }

    private void collectCallGraphMetrics(CallGraph callGraph, SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        int edgeOutCount = 0, edgeIntoCount = 0;
        Iterator<Edge> edgeOutIterator = callGraph.edgesOutOf(sootMethod);
        Iterator<Edge> edgeIntoIterator = callGraph.edgesInto(sootMethod);
        PaprikaClass currentClass = paprikaMethod.getPaprikaClass();

        while (edgeOutIterator.hasNext()) {
            Edge edgeOut = edgeOutIterator.next();
            SootMethod target = edgeOut.tgt();
            PaprikaMethod targetMethod = methodMap.get(target);
            // In the case we are calling an external method (sdk or library)
            if (targetMethod == null) {
                analyzeExternalCall(target, edgeOut, paprikaMethod);
            } else {
                paprikaMethod.callMethod(targetMethod);
            }
            PaprikaClass targetClass = container.getClassMap().get(target.getDeclaringClass());
            if (edgeOut.isVirtual() || edgeOut.isSpecial() || edgeOut.isStatic()) {
                edgeOutCount++;
            }
            // Detecting coupling (may include calls to inherited methods)
            if (targetClass != null && targetClass != currentClass) {
                currentClass.coupledTo(targetClass);
            }
        }

        while (edgeIntoIterator.hasNext()) {
            Edge e = edgeIntoIterator.next();
            if (e.isExplicit()) edgeIntoCount++;
        }
        NumberOfDirectCalls.createNumberOfDirectCalls(paprikaMethod, edgeOutCount);
        NumberOfCallers.createNumberOfCallers(paprikaMethod, edgeIntoCount);
    }

    private void analyzeExternalCall(SootMethod target, Edge edgeOut, PaprikaMethod paprikaMethod) {
        PaprikaExternalMethod externalTgtMethod = externalMethodMap.get(target);
        if (externalTgtMethod == null) {
            PaprikaExternalClass paprikaExternalClass = container.getOrCreateExternalClass(target.getDeclaringClass());
            externalTgtMethod = PaprikaExternalMethod.createPaprikaExternalMethod(target.getName(),
                    target.getReturnType().toString(), paprikaExternalClass);
            int i = 0;
            for (Type type : target.getParameterTypes()) {
                i++;
                PaprikaExternalArgument paprikaExternalArgument = PaprikaExternalArgument
                        .createPaprikaExternalArgument(type.toString(), i, externalTgtMethod);

            }
            externalMethodMap.put(target, externalTgtMethod);
        }
        paprikaMethod.callMethod(externalTgtMethod);
    }



}
