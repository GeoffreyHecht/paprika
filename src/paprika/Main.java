package paprika;

import paprika.analyzer.Analyzer;
import paprika.analyzer.SootAnalyzer;
import paprika.metrics.Metric;
import paprika.neo4j.ModelToGraph;

import java.util.List;

/**
 * Created by Geoffrey Hecht on 19/05/14.
 */

public class Main {
    private final static String ANDROID_JAR = "/home/geoffrey/These/decompiler/android-platforms";
    private final static String APK = "/home/geoffrey/These/decompiler/facebook.apk";
    private final static String DB_PATH = "/var/lib/neo4j/data/paprika.db";

    public static void main(String[] args) {

        Analyzer analyzer = new SootAnalyzer(APK, ANDROID_JAR);

        analyzer.init();
        analyzer.runAnalysis();
        List<? extends Metric> metrics = analyzer.getMetrics();

        for(Metric metric : metrics){
           //System.out.println(metric);
        }
       ModelToGraph modelToGraph = new ModelToGraph(DB_PATH);
       modelToGraph.insertApp(analyzer.getPaprikaApp());

    }
}
