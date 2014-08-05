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
    private  List<Metric> allMetrics;

    private String Rclass;

    public SootAnalyzer(String apk, String androidJAR,String name,String key,String pack,String date,int size,String dev,String cat,String price,double rating,String nbDownload) {
        Analyzer.apk = apk;
        this.androidJAR = androidJAR;
        this.paprikaApp = PaprikaApp.createPaprikaApp(name,key,pack,date,size,dev,cat,price,rating,nbDownload);
        this.Rclass = pack.concat(".R");
        this.classMap = new HashMap<>();
        this.methodMap = new HashMap<>();
        this.allMetrics = new ArrayList<>();
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
        allMetrics.addAll(collectAppMetrics());
        allMetrics.addAll(collectClassesMetrics());
        PackManager.v().getPack("gop").add(new Transform("gop.myInstrumenter", new BodyTransformer() {

            @Override
            protected void internalTransform(final Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                allMetrics.addAll(collectMethodsMetrics(body.getMethod()));
            }


        }));
        PackManager.v().runPacks();

        allMetrics.addAll(computeMetrics());
        //PackManager.v().writeOutput();

    }

    @Override
    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    @Override
    public List<? extends Metric> getMetrics() {
        return this.allMetrics;
    }

    public List<Metric> collectAppMetrics(){
        List<Metric> metrics = new ArrayList<>();
        Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
        metrics.add(NumberOfClasses.createNumberOfClasses(this.paprikaApp, sootClasses.size()));


        int activityCount = 0, serviceCount = 0, interfaceCount = 0, abstractCount = 0;
        for(SootClass sootClass : sootClasses){
            if(isActivity(sootClass)) activityCount++;
            else if(isService(sootClass)) serviceCount++;
            if(sootClass.isAbstract()) abstractCount++;
            else if(sootClass.isInterface()) interfaceCount++;
        }

        metrics.add(NumberOfActivities.createNumberOfActivities(this.paprikaApp, activityCount));
        metrics.add(NumberOfServices.createNumberOfServices(this.paprikaApp, serviceCount));
        metrics.add(NumberOfInterfaces.createNumberOfInterfaces(this.paprikaApp, interfaceCount));
        metrics.add(NumberOfAbstractClasses.createNumberOfAbstractClasses(this.paprikaApp, abstractCount));
        return metrics;
    }

    public List<? extends Metric> collectClassesMetrics(){
        List<Metric> metrics = new ArrayList<Metric>();
        Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
        for(SootClass sootClass : sootClasses){
            //Excluding R class from the analysis
            if(sootClass.getName().startsWith(Rclass)){
                //sootClass.setLibraryClass();
            }else{
                metrics.addAll(collectClassMetrics(sootClass));
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
        return metrics;
    }

    public List<? extends Metric> computeMetrics(){
        List<Metric> metrics = new ArrayList<Metric>();
        computeInheritance();
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
            // Create complexity with the final value
            metrics.add(ClassComplexity.createClassComplexity(paprikaClass));
            // Create NOC with the final value
            metrics.add(NumberOfChildren.createNumberOfChildren(paprikaClass));
            // CBO with final value
            metrics.add(CouplingBetweenObjects.createCouplingBetweenObjects(paprikaClass));
            //LCOM
            metrics.add(LackofCohesionInMethods.createLackofCohesionInMethods(paprikaClass));
        }
        return metrics;
    }
    public List<? extends Metric> collectMethodsMetrics(SootMethod sootMethod){
        List<Metric> metrics = new ArrayList<Metric>();
        SootClass sootClass = sootMethod.getDeclaringClass();
        PaprikaClass paprikaClass = classMap.get(sootClass);
        if (paprikaClass == null){
            LOGGER.warning("Class not analyzed : "+ sootClass);
            sootClass.setLibraryClass();
            return metrics;
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
        metrics.add(NumberOfParameters.createNumberOfParameters(paprikaMethod, sootMethod.getParameterCount()));
        if(sootMethod.hasActiveBody()){
            GrimpBody activeBody = (GrimpBody) sootMethod.getActiveBody();
            // Number of lines is the number of Units - number of Parameter - 1 (function name)
            int nbOfLines =  activeBody.getUnits().size() - sootMethod.getParameterCount() - 1;
            metrics.add(NumberOfDeclaredLocals.createNumberOfDeclaredLocals(paprikaMethod, activeBody.getLocals().size()));
            metrics.add(NumberOfInstructions.createNumberOfInstructions(paprikaMethod, nbOfLines));
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
            metrics.add(CyclomaticComplexity.createCyclomaticComplexity(paprikaMethod,nbOfBranches));
        }else{
            //LOGGER.info("No body for "+paprikaMethod);
        }
        metrics.addAll(collectMethodMetricsFromCallGraph(paprikaMethod,sootMethod));
        return metrics;
    }

    private List<? extends Metric> collectMethodMetricsFromCallGraph(PaprikaMethod paprikaMethod, SootMethod sootMethod) {
        List<UnaryMetric> unaryMetrics = new ArrayList<UnaryMetric>();
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
        unaryMetrics.add(NumberOfDirectCalls.createNumberOfDirectCalls(paprikaMethod, edgeOutCount));
        unaryMetrics.add(NumberOfCallers.createNumberOfCallers(paprikaMethod, edgeIntoCount));
        return unaryMetrics;
    }

    public List<? extends Metric> collectClassMetrics(SootClass sootClass){
        List<Metric> metrics = new ArrayList<>();
        PaprikaClass paprikaClass = PaprikaClass.createPaprikaClass(sootClass.getName(), this.paprikaApp);
        // Variable associated with classes
        for(SootField sootField : sootClass.getFields()){
            PaprikaModifiers modifiers = PaprikaModifiers.PRIVATE;
            if(sootField.isPublic()){
                modifiers = PaprikaModifiers.PUBLIC;
            }else if(sootField.isProtected()){
                modifiers = PaprikaModifiers.PROTECTED;
            }

            PaprikaVariable.createPaprikaVariable(sootField.getName(), sootField.getType().toString(), modifiers, paprikaClass);
        }
        this.classMap.put(sootClass, paprikaClass);
        // Number of methods including constructors
        metrics.add(NumberOfMethods.createNumberOfMethods(paprikaClass, sootClass.getMethodCount()));
        metrics.add(DepthOfInheritance.createDepthOfInheritance(paprikaClass, getDepthOfInheritance(sootClass)));
        metrics.add(NumberOfImplementedInterfaces.createNumberOfImplementedInterfaces(paprikaClass, sootClass.getInterfaceCount()));
        metrics.add(NumberOfAttributes.createNumberOfAttributes(paprikaClass, sootClass.getFieldCount()));
        return metrics;
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

    private boolean isSubClass(SootClass sootClass, String className){
        do{
            if(sootClass.getName().equals(className)) return true;
            sootClass = sootClass.getSuperclass();
        }while(sootClass.hasSuperclass());
        return false;
    }
}
