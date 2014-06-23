package paprika.analyzer;

import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaMethod;
import paprika.metrics.*;
import soot.*;
import soot.grimp.GrimpBody;
import soot.grimp.internal.GLookupSwitchStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.Chain;

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
    private  List<Metric> allMetrics;

    public SootAnalyzer(String apk, String androidJAR) {
        Analyzer.apk = apk;
        this.androidJAR = androidJAR;
    }

    @Override
    public void init() {
        //Hack to prevent soot to print on System.out
        PrintStream originalStream = System.out;
        /*System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                // NO-OP
            }
        }));*/

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
        Options.v().set_exclude(excludeList);
        //Options.v().set_no_bodies_for_excluded(true);
        //Options.v().setPhaseOption("cg","verbose:true");
        //Options.v().setPhaseOption("cg.cha", "on");
        Scene.v().loadNecessaryClasses();
        this.paprikaApp = PaprikaApp.createPaprikaApp(apk);
        this.classMap = new HashMap<SootClass, PaprikaClass>();
        this.allMetrics = new ArrayList<Metric>();
    }
    private static boolean done = false;
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
            metrics.addAll(collectClassMetrics(sootClass));
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
        for (PaprikaClass paprikaClass : paprikaApp.getPaprikaClasses()){
            // Create complexity with the final value
            metrics.add(ClassComplexity.createClassComplexity(paprikaClass));
            // Create NOC with the final value
            metrics.add(NumberOfChildren.createNumberOfChildren(paprikaClass));
        }
        return metrics;
    }
    public List<? extends Metric> collectMethodsMetrics(SootMethod sootMethod){
        List<Metric> metrics = new ArrayList<Metric>();
        SootClass sootClass = sootMethod.getDeclaringClass();
        PaprikaClass paprikaClass = classMap.get(sootClass);
        if (paprikaClass == null){
            paprikaClass = PaprikaClass.createPaprikaClass(sootClass.getName(), this.paprikaApp);
            classMap.put(sootClass, paprikaClass);
        }
        PaprikaMethod paprikaMethod = PaprikaMethod.createPaprikaMethod(sootMethod.getName(),paprikaClass);
        metrics.add(NumberOfParameters.createNumberOfParameters(paprikaMethod, sootMethod.getParameterCount()));
        if(sootMethod.hasActiveBody()){
            GrimpBody activeBody = (GrimpBody) sootMethod.getActiveBody();
            // Number of lines is the number of Units - number of Parameter - 1 (function name)
            int nbOfLines =  activeBody.getUnits().size() - sootMethod.getParameterCount() - 1;
            metrics.add(NumberOfDeclaredLocals.createNumberOfDeclaredLocals(paprikaMethod, activeBody.getLocals().size()));
            metrics.add(NumberOfInstructions.createNumberOfInstructions(paprikaMethod, nbOfLines));
            // Cyclomatic complexity
            int nbOfBranches = 1;
            for (Unit sootUnit : activeBody.getUnits()){
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
        //callGraph = null;
        while(edgeOutIterator.hasNext()) {
            Edge e = edgeOutIterator.next();
            if (e.isVirtual() || e.isSpecial() || e.isStatic()) edgeOutCount++;
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
