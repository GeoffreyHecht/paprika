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
import paprika.analyse.metrics.common.*;
import paprika.analyse.metrics.methods.NumberOfCallers;
import paprika.analyse.metrics.methods.NumberOfDirectCalls;
import paprika.analyse.metrics.methods.condition.IsSynchronized;
import paprika.analyse.metrics.methods.stat.NumberOfParameters;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Iterator;
import java.util.Map;

/**
 * Processes the methods of an application after the call graph has been built.
 */
public class MethodProcessor {

    private PaprikaContainer container;
    private Map<SootMethod, PaprikaMethod> methodMap;
    private Map<SootMethod, PaprikaExternalMethod> externalMethodMap;
    private MethodBodyProcessor bodyProcessor;

    private CommonCondition[] commonConditions = {
            new IsStatic(),
            new IsFinal(),
            new IsAbstract()
    };

    private IsSynchronized isSynchronized;
    private NumberOfParameters parameters;


    /**
     * Constructor.
     *
     * @param container the container of the analyzed application
     */
    public MethodProcessor(PaprikaContainer container) {
        this.container = container;
        this.methodMap = container.getMethodMap();
        this.externalMethodMap = container.getExternalMethodMap();
        this.isSynchronized = new IsSynchronized();
        this.parameters = new NumberOfParameters();
        this.bodyProcessor = new MethodBodyProcessor();
    }

    /**
     * Processes all methods of an application. Must be called only after the Soot call graph has
     * been built and all methods have been registered in the Paprika container with
     * {@link PaprikaContainer#addMethod(SootMethod)}
     */
    public void processMethods() {
        CallGraph callGraph = Scene.v().getCallGraph();
        for (Map.Entry<SootMethod, PaprikaMethod> entry : methodMap.entrySet()) {
            collectStandardMetrics(entry.getKey(), entry.getValue());
            collectCallGraphMetrics(callGraph, entry.getKey(), entry.getValue());
        }
        NumberOfMethods.createNumberOfMethods(container.getPaprikaApp(), methodMap.size());
    }

    /**
     * Collect metrics for a method that do not depend on the call graph.
     *
     * @param sootMethod    the Soot representation of the method
     * @param paprikaMethod the Paprika representation of the method
     */
    private void collectStandardMetrics(SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        for (CommonCondition condition : commonConditions) {
            condition.createIfMatching(sootMethod, paprikaMethod);
        }
        isSynchronized.createIfMatching(sootMethod, paprikaMethod);
        parameters.collectMetric(sootMethod, paprikaMethod);
        if (sootMethod.hasActiveBody()) {
            bodyProcessor.processMethodBody(sootMethod, paprikaMethod);
        }
    }

    /**
     * Collect method metrics related to the Soot call graph.
     *
     * @param callGraph     the Soot call graph
     * @param sootMethod    the Soot representation of the method
     * @param paprikaMethod the Paprika representation of the method
     */
    private void collectCallGraphMetrics(CallGraph callGraph, SootMethod sootMethod, PaprikaMethod paprikaMethod) {
        int edgeOutCount = 0;
        int edgeIntoCount = 0;
        Iterator<Edge> edgeOutIterator = callGraph.edgesOutOf(sootMethod);
        Iterator<Edge> edgeIntoIterator = callGraph.edgesInto(sootMethod);
        PaprikaClass currentClass = paprikaMethod.getPaprikaClass();

        while (edgeOutIterator.hasNext()) {
            Edge edgeOut = edgeOutIterator.next();
            SootMethod target = edgeOut.tgt();
            PaprikaMethod targetMethod = methodMap.get(target);
            // In the case we are calling an external method (sdk or library)
            if (targetMethod == null) {
                analyzeExternalCall(target, paprikaMethod);
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
        NumberOfDirectCalls.createMetric(paprikaMethod, edgeOutCount);
        NumberOfCallers.createMetric(paprikaMethod, edgeIntoCount);
    }

    /**
     * Add an external call to a library to the Paprika application model.
     *
     * @param target        the Soot representation of the library method that was called
     * @param paprikaMethod the Paprika method calling the library method
     */
    private void analyzeExternalCall(SootMethod target, PaprikaMethod paprikaMethod) {
        PaprikaExternalMethod externalTgtMethod = externalMethodMap.get(target);
        if (externalTgtMethod == null) {
            PaprikaExternalClass paprikaExternalClass = container.getOrCreateExternalClass(target.getDeclaringClass());
            externalTgtMethod = PaprikaExternalMethod.create(target.getName(),
                    target.getReturnType().toString(), paprikaExternalClass);
            int i = 0;
            for (Type type : target.getParameterTypes()) {
                i++;
                PaprikaExternalArgument.create(type.toString(), i, externalTgtMethod);
            }
            externalMethodMap.put(target, externalTgtMethod);
        }
        paprikaMethod.callMethod(externalTgtMethod);
    }


}
