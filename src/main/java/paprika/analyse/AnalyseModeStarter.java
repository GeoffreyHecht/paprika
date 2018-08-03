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

import paprika.analyse.analyzer.AnalyzerException;
import paprika.analyse.analyzer.SootAnalyzer;
import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.neo4j.ModelToGraph;
import paprika.launcher.PaprikaStarter;
import paprika.launcher.arg.PaprikaArgParser;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static paprika.launcher.arg.Argument.*;

public class AnalyseModeStarter extends PaprikaStarter {

    private static final int SOOT_RETRIES = 3;

    public AnalyseModeStarter(PaprikaArgParser argParser, PrintStream out) {
        super(argParser, out);
    }

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
            }
        } else {
            processApp(argParser.getArg(APK_ARG), modelToGraph, null);
            out.println("Done");
        }
    }

    private void processApp(String apk, ModelToGraph modelToGraph, @Nullable ApkPropertiesParser propsParser) {
        try {
            PaprikaApp app = analyzeApp(apk, propsParser);
            saveIntoDatabase(app, modelToGraph);
        } catch (AnalyzerException e) {
            notifyAnalysisFailure(apk, e);
        }
    }

    private void notifyAnalysisFailure(String apk, AnalyzerException e) {
        out.println("Failed to analyze " + apk);
        e.printStackTrace(out);
    }

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
            analyzer.prepareSoot();
            creator.readAppInfo();
            creator.fetchMissingAppInfo();
            if (propsParser != null) {
                creator.addApkProperties(propsParser);
            }
            try {
                analyzer.runAnalysis(creator.createApp(), argParser.getFlagArg(ONLY_MAIN_PACKAGE_ARG));
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

    public void saveIntoDatabase(PaprikaApp app, ModelToGraph modelToGraph) {
        out.println("Saving into database " + argParser.getArg(DATABASE_ARG));
        modelToGraph.insertApp(app);
    }


}
