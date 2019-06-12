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

package paprika.analyse;

import org.neo4j.graphdb.TransactionFailureException;
import paprika.analyse.analyzer.AnalyzerException;
import paprika.analyse.analyzer.SootAnalyzer;
import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.neo4j.ModelToGraph;
import paprika.launcher.PaprikaStarter;
import paprika.launcher.arg.PaprikaArgParser;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static paprika.launcher.arg.Argument.*;

/**
 * Starts and manages a Paprika analysis session.
 */
public class AnalyseModeStarter extends PaprikaStarter {

    private static final int SOOT_RETRIES = 3;
    private static final int NEO4J_RETRIES = 3;

    private PrintStream originalOut;
    private PrintStream originalErr;

    /**
     * Constructor. Mutes System.out and System.err standard outputs until the method
     * {@link #start()} is done executing.
     *
     * @param argParser the argument manager for this session
     * @param out       a stream to display user feedback
     */
    public AnalyseModeStarter(PaprikaArgParser argParser, PrintStream out) {
        super(argParser, out);
        // Hack to prevent soot from spamming System.out and System.err
        originalOut = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                // NO-OP
            }
        }));
        originalErr = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // NO-OP
            }
        }));
    }

    /**
     * Start the analysis of one or multiple apks.
     * Restores System.out and System.err once done.
     */
    @Override
    public void start() {
        ModelToGraph modelToGraph = new ModelToGraph(argParser.getArg(DATABASE_ARG));
        List<String> appsPaths = argParser.getAppsPaths();
        if (argParser.isFolderMode()) {
            out.println("Analyzing all apps in folder " + argParser.getArg(APK_ARG));
            ApkPropertiesParser propsParser = null;
            try {
                propsParser = new ApkPropertiesParser(out, argParser.getArg(APK_ARG) + "/"
                        + ApkPropertiesParser.PROPS_FILENAME, argParser.getAppsFilenames(appsPaths));
                propsParser.readProperties();
            } catch (PropertiesException | IOException e) {
                e.printStackTrace(out);
            }
            for (String apk : appsPaths) {
                processApp(apk, modelToGraph, propsParser);
                out.println("Done");
                restoreDefaultStreams();
            }
        } else {
            processApp(argParser.getArg(APK_ARG), modelToGraph, null);
            out.println("Done");
            restoreDefaultStreams();
        }
    }

    private void restoreDefaultStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void processApp(String apk, ModelToGraph modelToGraph, @Nullable ApkPropertiesParser propsParser) {
        try {
            PaprikaApp app = analyzeApp(apk, propsParser);
            saveIntoDatabase(app, modelToGraph, 0);
        } catch (AnalyzerException e) {
            notifyAnalysisFailure(apk, e);
        }
    }

    /**
     * Analyze a single apk and convert it to its corresponding Paprika application model.
     *
     * @param apkPath     the path to the Android apk
     * @param propsParser the json parser used to insert app properties such as a key or a name,
     *                    or null if no such parsing is to be done
     * @return a PaprikaApp corresponding to the given apk
     * @throws AnalyzerException if Soot fails to analyze the apk
     */
    public PaprikaApp analyzeApp(String apkPath, @Nullable ApkPropertiesParser propsParser)
            throws AnalyzerException {
        return analyze(apkPath, propsParser, 0);
    }

    private PaprikaApp analyze(String apkPath, @Nullable ApkPropertiesParser propsParser, int retries)
            throws AnalyzerException {
        try {
            out.println("Analyzing " + new File(apkPath).getName());
            out.println("Collecting metrics");
            SootAnalyzer analyzer = new SootAnalyzer(apkPath, argParser.getArg(ANDROID_JARS_ARG));
            PaprikaAppCreator creator = new PaprikaAppCreator(argParser, apkPath);
            try {
                analyzer.prepareSoot();
            } catch (RuntimeException e) {
                out.println("Soot could not parse the path of the apk.");
                throw new AnalyzerException(apkPath, e);
            }
            creator.readAppInfo();
            try {
                creator.fetchMissingAppInfo();
            } catch (NumberFormatException e) {
                out.println("The Android minimum or target sdk could not be parsed");
                throw new AnalyzerException(apkPath, e);
            }
            if (propsParser != null) {
                creator.addApkProperties(propsParser);
            }
            PaprikaApp app = creator.createApp();
            if (!argParser.getFlagArg(FORCE_ANALYSIS_ARG)
                    && (app.getTargetSdkVersion() >= 26 || app.getSdkVersion() >= 26)) {
                out.println("As of 08/2018, Soot has issues analyzing apps using an sdk >= 26");
                out.println("The app " + apkPath + " will not be analyzed.");
                throw new AnalyzerException(apkPath);
            }
            try {
                analyzer.runAnalysis(app, argParser.getFlagArg(ONLY_MAIN_PACKAGE_ARG));
            } catch (RuntimeException e) {
                if (retries < SOOT_RETRIES) {
                    // Soot, please stop crashing randomly. We'll try this again.
                    out.println("Encountered soot issue on app " + apkPath);
                    out.println("Restarting soot analysis...");
                    return analyze(apkPath, propsParser, retries + 1);
                } else {
                    out.println("Soot could not analyze " + apkPath);
                    throw new AnalyzerException(apkPath, e);
                }
            }
            return analyzer.getPaprikaApp();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new AnalyzerException(apkPath, e);
        }
    }

    private void saveIntoDatabase(PaprikaApp app, ModelToGraph modelToGraph, int retries) {
        try {
            out.println("Saving into database " + argParser.getArg(DATABASE_ARG));
            modelToGraph.insertApp(app);
        } catch (TransactionFailureException e) {
            if (retries < NEO4J_RETRIES) {
                out.println("Failed to insert into database");
                out.println("Trying again...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interrupt) {
                    out.println("Interrupted while waiting to try a new transaction");
                    Thread.currentThread().interrupt();
                }
                saveIntoDatabase(app, modelToGraph, retries + 1);
            }
        }
    }

    private void notifyAnalysisFailure(String apk, Exception e) {
        out.println("Failed to analyze " + apk);
        if (e.getCause() != null) {
            out.println(e.getCause().getMessage());
        }
    }

}
