package paprika.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaClass extends Entity{
    private PaprikaApp paprikaApp;
    private PaprikaClass parent;
    private int complexity;
    private int children;
    private Set<PaprikaClass> coupled;
    private Set<PaprikaMethod> paprikaMethods;
    private Set<PaprikaVariable> paprikaVariables;
    private PaprikaModifiers modifier;

    public PaprikaModifiers getModifier() {
        return modifier;
    }

    public Set<PaprikaVariable> getPaprikaVariables() {
        return paprikaVariables;
    }

    public Set<PaprikaMethod> getPaprikaMethods() {
        return paprikaMethods;
    }

    private PaprikaClass(String name, PaprikaApp paprikaApp, PaprikaModifiers modifier) {
        this.setName(name);
        this.paprikaApp = paprikaApp;
        this.complexity = 0;
        this.children = 0;
        this.paprikaMethods  = new HashSet<>();
        this.paprikaVariables = new HashSet<>();
        this.coupled = new HashSet<>();
    }

    public static PaprikaClass createPaprikaClass(String name, PaprikaApp paprikaApp, PaprikaModifiers modifier) {
        PaprikaClass paprikaClass = new PaprikaClass(name, paprikaApp, modifier);
        paprikaApp.addPaprikaClass(paprikaClass);
        return paprikaClass;
    }

    public PaprikaClass getParent() {
        return parent;
    }

    public void setParent(PaprikaClass parent) {
        this.parent = parent;
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

    public void coupledTo(PaprikaClass paprikaClass){ coupled.add(paprikaClass);}

    //TODO: Eventually we can substract the DepthOfInheritance if necessary
    public int getCouplingValue(){ return coupled.size();}

    public int computeLCOM(){
        Object methods[] = paprikaMethods.toArray();
        int methodCount = methods.length;
        int haveFieldInCommon = 0;
        int noFieldInCommon  = 0;
        for(int i=0; i< methodCount;i++){
            for(int j=i+1; j < methodCount; j++){
                if( ((PaprikaMethod) methods[i]).haveCommonFields((PaprikaMethod) methods[j])){
                    haveFieldInCommon++;
                }else{
                    noFieldInCommon++;
                }
            }
        }
        int LCOM =  noFieldInCommon - haveFieldInCommon;
        return LCOM > 0 ? LCOM : 0;
    }

    public void addPaprikaVariable(PaprikaVariable paprikaVariable) {
        paprikaVariables.add(paprikaVariable);
    }

    public PaprikaVariable findVariable(String name){
        // First we are looking to the field declared by this class (any modifiers)
        for (PaprikaVariable paprikaVariable : paprikaVariables){
            if (paprikaVariable.getName().equals(name)) return paprikaVariable;
        }
        //otherwise we return null
        return null;
    }
}
