package paprika.analyzer;

import paprika.entities.*;
import paprika.metrics.*;
import soot.*;
import soot.grimp.GrimpBody;
import soot.grimp.internal.GInstanceFieldRef;
import soot.grimp.internal.GLookupSwitchStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.Chain;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class SootAnalyzer extends Analyzer {
    private final static Logger LOGGER = Logger.getLogger(SootAnalyzer.class.getName());

    private static String androidJAR;
    private PaprikaApp paprikaApp;
    private Map<SootClass,PaprikaClass>classMap;
    private Map<SootMethod,PaprikaMethod>methodMap;
    int activityCount = 0, serviceCount = 0, interfaceCount = 0, abstractCount = 0, broadcastReceiverCount = 0, contentProviderCount = 0;
    private String rClass;

    public SootAnalyzer(String apk, String androidJAR,String name,String key,String pack,String date,int size,String dev,String cat,String price,double rating,String nbDownload) {
        Analyzer.apk = apk;
        this.androidJAR = androidJAR;
        this.paprikaApp = PaprikaApp.createPaprikaApp(name,key,pack,date,size,dev,cat,price,rating,nbDownload);
        this.rClass = pack.concat(".R");
        this.classMap = new HashMap<>();
        this.methodMap = new HashMap<>();
    }

    @Override
    public void init() {
        //Hack to prevent soot to print on System.out
        PrintStream originalStream = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                // NO-OP
            }
        }));

        G.reset();
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        //Path to android-sdk-platforms
        Options.v().set_android_jars(androidJAR);
        //prefer Android APK files
        Options.v().set_src_prec(Options.src_prec_apk);
        // Allow phantom references
        Options.v().set_allow_phantom_refs(true);
        //Set path to APK
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_whole_program(true);
        Options.v().set_output_format(Options.output_format_grimple);
        Options.v().set_output_dir("/home/geoffrey/These/decompiler/outSnake");
        //Options.v().set_soot_classpath();

        PhaseOptions.v().setPhaseOption("gop", "enabled:true");
        System.setOut(originalStream);

        //Options.v().set_soot_classpath(Scene.v().getAndroidJarPath(androidJAR,apk));

        List<String> excludeList = new LinkedList<String>();
        excludeList.add("java.");
        excludeList.add("sun.misc.");
        excludeList.add("android.");
        excludeList.add("org.apache.");
        excludeList.add("javax.");
        //excludeList.add("com.example.myapplication3.app.r");
        Options.v().set_exclude(excludeList);
        //Options.v().set_no_bodies_for_excluded(true);
        //Options.v().setPhaseOption("cg","verbose:true");
        //Options.v().setPhaseOption("cg.cha", "on");

        Scene.v().loadNecessaryClasses();
    }

    @Override
    public void runAnalysis() {
        collectClassesMetrics();
        collectAppMetrics();
        PackManager.v().getPack("gop").add(new Transform("gop.myInstrumenter", new BodyTransformer() {

            @Override
            protected void internalTransform(final Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                collectMethodsMetrics(body.getMethod());
            }


        }));
        PackManager.v().runPacks();
        computeMetrics();
        //PackManager.v().writeOutput();

    }

    @Override
    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    /**
     * Should be called after all classes have been processed once
     */
    public void collectAppMetrics(){
        Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
        NumberOfClasses.createNumberOfClasses(this.paprikaApp, sootClasses.size());
        NumberOfActivities.createNumberOfActivities(this.paprikaApp, activityCount);
        NumberOfServices.createNumberOfServices(this.paprikaApp, serviceCount);
        NumberOfInterfaces.createNumberOfInterfaces(this.paprikaApp, interfaceCount);
        NumberOfAbstractClasses.createNumberOfAbstractClasses(this.paprikaApp, abstractCount);
        NumberOfBroadcastReceivers.createNumberOfBroadcastReceivers(this.paprikaApp, broadcastReceiverCount);
        NumberOfContentProviders.createNumberOfContentProviders(this.paprikaApp, contentProviderCount);
    }

    public void collectClassesMetrics(){
        Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
        for(SootClass sootClass : sootClasses){
            //Excluding R class from the analysis
            if(sootClass.getName().startsWith(rClass)){
                //sootClass.setLibraryClass();
            }else{
                collectClassMetrics(sootClass);
            }
        }
        // Now that all classes have been processed at least once (and the map filled) we can process NOC
        for(SootClass sootClass : sootClasses){
            if(sootClass.hasSuperclass()){
                SootClass superClass = sootClass.getSuperclass();
                PaprikaClass paprikaClass = classMap.get(superClass);
                if(paprikaClass !=  null) classMap.get(superClass).addChildren();
            }
        }
    }

    /**
     * Should be called last
     */
    public void computeMetrics(){
        computeInheritance();
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
            // Create complexity with the final value
            ClassComplexity.createClassComplexity(paprikaClass);
            // Create NOC with the final value
            NumberOfChildren.createNumberOfChildren(paprikaClass);
            // CBO with final value
            CouplingBetweenObjects.createCouplingBetweenObjects(paprikaClass);
            //LCOM
            LackofCohesionInMethods.createLackofCohesionInMethods(paprikaClass);
        }
    }

    /**
     * Should be called after all classes have been processed once
     */
    public void collectMethodsMetrics(SootMethod sootMethod){
        SootClass sootClass = sootMethod.getDeclaringClass();
        PaprikaClass paprikaClass = classMap.get(sootClass);
        if (paprikaClass == null){
            //Should be R class
            LOGGER.warning("Class not analyzed : "+ sootClass);
            sootClass.setLibraryClass();
            return;
            /*
            paprikaClass = PaprikaClass.createPaprikaClass(sootClass.getName(), this.paprikaApp);
            classMap.put(sootClass, paprikaClass);
            */
        }
        PaprikaModifiers modifiers = PaprikaModifiers.PRIVATE;
        if(sootMethod.isPublic()){
            modifiers = PaprikaModifiers.PUBLIC;
        }else if(sootMethod.isProtected()){
            modifiers = PaprikaModifiers.PROTECTED;
        }

        PaprikaMethod paprikaMethod = PaprikaMethod.createPaprikaMethod(sootMethod.getName(),modifiers,sootMethod.getReturnType().toString(),paprikaClass);
        methodMap.put(sootMethod, paprikaMethod);
        if(sootMethod.isStatic()){
            IsStatic.createIsStatic(paprikaMethod, true);
        }
        if(sootMethod.isFinal()){
            IsFinal.createIsFinal(paprikaMethod, true);
        }
        if(sootMethod.isSynchronized()){
            IsSynchronized.createIsSynchronized(paprikaMethod, true);
        }
        if(sootMethod.isAbstract()){
            IsAbstract.createIsAbstract(paprikaMethod, true);
        }
        NumberOfParameters.createNumberOfParameters(paprikaMethod, sootMethod.getParameterCount());
        if(sootMethod.hasActiveBody()){
            GrimpBody activeBody = (GrimpBody) sootMethod.getActiveBody();
            // Number of lines is the number of Units - number of Parameter - 1 (function name)
            int nbOfLines =  activeBody.getUnits().size() - sootMethod.getParameterCount() - 1;
            NumberOfDeclaredLocals.createNumberOfDeclaredLocals(paprikaMethod, activeBody.getLocals().size());
            NumberOfInstructions.createNumberOfInstructions(paprikaMethod, nbOfLines);
            // Cyclomatic complexity & Lack of Cohesion methods
            int nbOfBranches = 1;
            for (Unit sootUnit : activeBody.getUnits()){
                //LCOM
                List<ValueBox> boxes = sootUnit.getUseAndDefBoxes();
                for (ValueBox valueBox : boxes){
                    Value value = valueBox.getValue();
                    if (value instanceof GInstanceFieldRef) {
                        SootFieldRef field = ((GInstanceFieldRef) value).getFieldRef();
                        if(field.declaringClass() == sootClass){
                            PaprikaVariable paprikaVariable = paprikaClass.findVariable(field.name());
                            //If we don't find the field it's inherited and thus not used for LCOM2
                            if(paprikaVariable != null){
                                paprikaMethod.useVariable(paprikaVariable);
                            }
                        }
                    }
                }
                //Cyclomatic complexity
                if (sootUnit.branches()){
                    if(sootUnit.fallsThrough()) nbOfBranches++;
                    else if(sootUnit instanceof GLookupSwitchStmt) nbOfBranches += ((GLookupSwitchStmt) sootUnit).getLookupValues().size();
                }

            }
            CyclomaticComplexity.createCyclomaticComplexity(paprikaMethod, nbOfBranches);
        }else{
            //LOGGER.info("No body for "+paprikaMethod);
        }
        collectMethodMetricsFromCallGraph(paprikaMethod, sootMethod);
    }

    private void collectMethodMetricsFromCallGraph(PaprikaMethod paprikaMethod, SootMethod sootMethod) {
        CallGraph callGraph = Scene.v().getCallGraph();
        int edgeOutCount = 0, edgeIntoCount = 0;
        Iterator<Edge> edgeOutIterator = callGraph.edgesOutOf(sootMethod);
        Iterator<Edge> edgeIntoIterator = callGraph.edgesInto(sootMethod);
        callGraph = null;
        PaprikaClass currentClass = paprikaMethod.getPaprikaClass();
        while(edgeOutIterator.hasNext()) {
            Edge e = edgeOutIterator.next();
            PaprikaMethod targetMethod =  methodMap.get(e.tgt());
            if(targetMethod != null){
                paprikaMethod.callMethod(targetMethod);
            }
            PaprikaClass targetClass = classMap.get(e.tgt().getDeclaringClass());
            if (e.isVirtual() || e.isSpecial() || e.isStatic()) edgeOutCount++;
            //Detecting coupling (may include calls to inherited methods)
            if (targetClass != null && targetClass != currentClass) currentClass.coupledTo(targetClass);
        }
        while(edgeIntoIterator.hasNext()){
            Edge e = edgeIntoIterator.next();
            if (e.isExplicit()) edgeIntoCount++;
        }
        NumberOfDirectCalls.createNumberOfDirectCalls(paprikaMethod, edgeOutCount);
        NumberOfCallers.createNumberOfCallers(paprikaMethod, edgeIntoCount);
    }

    public void collectClassMetrics(SootClass sootClass){
        PaprikaModifiers modifier = PaprikaModifiers.PRIVATE;
        if(sootClass.isPublic()){
            modifier = PaprikaModifiers.PUBLIC;
        }else if(sootClass.isProtected()){
            modifier = PaprikaModifiers.PROTECTED;
        }

        PaprikaClass paprikaClass = PaprikaClass.createPaprikaClass(sootClass.getName(), this.paprikaApp, modifier);
        if(sootClass.isStatic()){
            IsStatic.createIsStatic(paprikaClass, true);
        }
        if(sootClass.isFinal()){
            IsFinal.createIsFinal(paprikaClass, true);
        }
        if(sootClass.isInnerClass()){
            IsInnerClass.createIsInnerClass(paprikaClass, true);
        }
        if(isActivity(sootClass)){
            activityCount++;
            IsActivity.createIsActivity(paprikaClass,true);
        }
        else if(isService(sootClass)){
            serviceCount++;
            IsService.createIsService(paprikaClass,true);
        }
        else if(isBroadcastReceiver(sootClass)){
            broadcastReceiverCount++;
            IsBroadcastReceiver.createIsBroadcastReceiver(paprikaClass,true);
        }
        else if(isContentProvider(sootClass)){
            contentProviderCount++;
            IsContentProvider.createIsContentProvider(paprikaClass,true);
        }else if(isApplication(sootClass)){
            IsApplication.createIsApplication(paprikaClass,true);
        }
        if(sootClass.isAbstract()){
            abstractCount++;
            IsAbstract.createIsAbstract(paprikaClass, true);
        }
        if(sootClass.isInterface()){
            interfaceCount++;
            IsInterface.createIsInterface(paprikaClass, true);
        }
        // Variable associated with classes
        for(SootField sootField : sootClass.getFields()){
            modifier = PaprikaModifiers.PRIVATE;
            if(sootField.isPublic()){
                modifier = PaprikaModifiers.PUBLIC;
            }else if(sootField.isProtected()){
                modifier = PaprikaModifiers.PROTECTED;
            }
            PaprikaVariable paprikaVariable = PaprikaVariable.createPaprikaVariable(sootField.getName(), sootField.getType().toString(), modifier, paprikaClass);
            if(sootField.isStatic()){
                IsStatic.createIsStatic(paprikaVariable, true);
            }
            if(sootField.isFinal()){
                IsFinal.createIsFinal(paprikaVariable, true);
            }
        }
        this.classMap.put(sootClass, paprikaClass);
        // Number of methods including constructors
        NumberOfMethods.createNumberOfMethods(paprikaClass, sootClass.getMethodCount());
        DepthOfInheritance.createDepthOfInheritance(paprikaClass, getDepthOfInheritance(sootClass));
        NumberOfImplementedInterfaces.createNumberOfImplementedInterfaces(paprikaClass, sootClass.getInterfaceCount());
        NumberOfAttributes.createNumberOfAttributes(paprikaClass, sootClass.getFieldCount());
    }

    public int getDepthOfInheritance(SootClass sootClass){
        int doi = 0;
        do{
            doi++;
            sootClass = sootClass.getSuperclass();
        }while(sootClass.hasSuperclass());
        return doi;
    }

    public void computeInheritance(){
        for (Map.Entry entry : classMap.entrySet()) {
            SootClass sClass = (SootClass) entry.getKey();
            PaprikaClass pClass = (PaprikaClass) entry.getValue();
            SootClass sParent = sClass.getSuperclass();
            PaprikaClass pParent  = classMap.get(sParent);
            if(pParent != null){
               pClass.setParent(pParent);
            }
        }
    }
    private boolean isActivity(SootClass sootClass){
        return isSubClass(sootClass,"android.app.Activity");
    }

    private boolean isService(SootClass sootClass){
        return isSubClass(sootClass,"android.app.Service");
    }

    private boolean isBroadcastReceiver(SootClass sootClass){ return isSubClass(sootClass,"android.content.BroadcastReceiver");}

    private boolean isContentProvider(SootClass sootClass){ return isSubClass(sootClass,"android.content.ContentProvider");}

    private boolean isApplication(SootClass sootClass){ return isSubClass(sootClass,"android.app.Application");}

    private boolean isSubClass(SootClass sootClass, String className){
        do{
            if(sootClass.getName().equals(className)) return true;
            sootClass = sootClass.getSuperclass();
        }while(sootClass.hasSuperclass());
        return false;
    }
}
