package paprika.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaExternalClass extends Entity{
    private PaprikaApp paprikaApp;
    private String parentName;
    private Set<PaprikaExternalMethod> paprikaExternalMethods;

    public Set<PaprikaExternalMethod> getPaprikaExternalMethods() {
        return paprikaExternalMethods;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    private PaprikaExternalClass(String name, PaprikaApp paprikaApp) {
        this.setName(name);
        this.paprikaApp = paprikaApp;
        this.paprikaExternalMethods  = new HashSet<>();
    }

    public static PaprikaExternalClass createPaprikaExternalClass(String name, PaprikaApp paprikaApp) {
        PaprikaExternalClass paprikaClass = new PaprikaExternalClass(name, paprikaApp);
        paprikaApp.addPaprikaExternalClass(paprikaClass);
        return paprikaClass;
    }

    public void addPaprikaExternalMethod(PaprikaExternalMethod paprikaMethod){
        paprikaExternalMethods.add(paprikaMethod);
    }

    public PaprikaApp getPaprikaApp() {
        return paprikaApp;
    }

    public void setPaprikaApp(PaprikaApp paprikaApp) {
        this.paprikaApp = paprikaApp;
    }

}
