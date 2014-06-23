package paprika.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaClass extends Entity{
    private PaprikaApp paprikaApp;
    private int complexity;
    private int children;

    public List<PaprikaMethod> getPaprikaMethods() {
        return paprikaMethods;
    }

    private List<PaprikaMethod> paprikaMethods;

    private PaprikaClass(String name, PaprikaApp paprikaApp) {
        this.setName(name);
        this.paprikaApp = paprikaApp;
        this.complexity = 0;
        this.children = 0;
        this.paprikaMethods  = new ArrayList<PaprikaMethod>();
    }

    public static PaprikaClass createPaprikaClass(String name, PaprikaApp paprikaApp) {
        PaprikaClass paprikaClass = new PaprikaClass(name, paprikaApp);
        paprikaApp.addPaprikaClass(paprikaClass);
        return paprikaClass;
    }

    public void addPaprikaMethod(PaprikaMethod paprikaMethod){
        paprikaMethods.add(paprikaMethod);
    }

    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    public void setPaprikaApp(PaprikaApp paprikaApp) {
        this.paprikaApp = paprikaApp;
    }

    public void addComplexity(int value){
        complexity += value;
    }

    public void addChildren() { children += 1;}

    public int getComplexity() {
        return complexity;
    }

    public int getChildren() { return children; }

}
