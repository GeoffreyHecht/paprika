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

import paprika.entities.PaprikaApp;
import paprika.metrics.classes.stat.paprika.*;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class SootAnalyzer extends Analyzer {

    private String androidJAR;
    private PaprikaContainer container;

    private MethodProcessor methodProcessor;
    private ClassProcessor classProcessor;

    private List<PaprikaClassStatistic> finalMetrics = Arrays.asList(
            new ClassComplexity(),
            new CouplingBetweenObjects(),
            new LackOfCohesionInMethods(),
            new NPathComplexity(), // Must be done after class complexity has been processed
            new NumberOfChildren()
    );

    public SootAnalyzer(String apk, String androidJAR, String name, String key, String pack, String date, int size,
                        String dev, String cat, String price, double rating, int nbDownload, String versionCode,
                        String versionName, int sdkVersion, int targetSdkVersion, boolean mainPackageOnly) {
        this.apk = apk;
        this.androidJAR = androidJAR;
        this.container = new PaprikaContainer(PaprikaApp.createPaprikaApp(name, key, pack, date, size, dev, cat, price, rating,
                nbDownload, versionCode, versionName, sdkVersion, targetSdkVersion));
        this.classProcessor = new ClassProcessor(container, mainPackageOnly);
        this.methodProcessor = new MethodProcessor(container);
    }

    @Override
    public void prepareSoot() {
        //Hack to prevent soot to print on System.out
        PrintStream originalStream = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                // NO-OP
            }
        }));
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_android_jars(androidJAR);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_output_format(Options.output_format_grimple);
        Options.v().set_output_dir(System.getProperty("user.home") + File.separator + "/These/decompiler/out");
        Options.v().set_process_multiple_dex(true);
        Options.v().set_throw_analysis(Options.throw_analysis_dalvik);
        Options.v().set_no_bodies_for_excluded(true);
        PhaseOptions.v().setPhaseOption("cg", "all-reachable:true");
        PhaseOptions.v().setPhaseOption("gop", "enabled:true");
        List<String> excludeList = new LinkedList<>();
        excludeList.add("java.*");
        excludeList.add("sun.misc.*");
        excludeList.add("android.*");
        excludeList.add("org.apache.*");
        excludeList.add("soot.*");
        excludeList.add("javax.servlet.*");
        Options.v().set_exclude(excludeList);
        Scene.v().loadNecessaryClasses();
        System.setOut(originalStream);
    }

    @Override
    public void runAnalysis() throws IOException {
        container.readMissingAppInfo(apk);
        classProcessor.processClasses();
        PackManager.v().getPack("gop").add(new Transform("gop.myInstrumenter", new BodyTransformer() {
            @Override
            protected void internalTransform(final Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                container.addMethod(body.getMethod());
            }
        }));
        PackManager.v().runPacks();
        methodProcessor.processMethods();
        computeFinalMetrics();
    }


    @Override
    public PaprikaApp getPaprikaApp() {
        return container.getPaprikaApp();
    }

    /**
     * Should be called last
     */
    public void computeFinalMetrics() {
        container.computeInheritance();
        container.computeInterface();
        container.getPaprikaApp().getPaprikaClasses()
                .forEach(paprikaClass -> finalMetrics
                        .forEach(metric -> metric.collectMetric(paprikaClass)));
    }


}
