package paprika.analyzer;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public abstract class Analyzer {
    protected static String apk;

    public abstract void init();
    public abstract void runAnalysis();
    public abstract PaprikaApp getPaprikaApp();
}
