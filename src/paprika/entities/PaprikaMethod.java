package paprika.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaMethod extends Entity{
    private PaprikaClass paprikaClass;
    private String returnType;
    private Set<PaprikaVariable> usedVariables;
    private Set<PaprikaMethod> calledMethods;
    private PaprikaModifiers modifier;
    private List<PaprikaArgument> arguments;
    //TODO overriding
    public String getReturnType() {
        return returnType;
    }

    public PaprikaModifiers getModifier() {
        return modifier;
    }

    private PaprikaMethod(String name, PaprikaModifiers modifier, String returnType, PaprikaClass paprikaClass) {
        this.setName(name);
        this.paprikaClass = paprikaClass;
        this.usedVariables = new HashSet<>();
        this.calledMethods = new HashSet<>();
        this.arguments = new ArrayList<>();
        this.modifier = modifier;
        this.returnType = returnType;
    }

    public static PaprikaMethod createPaprikaMethod(String name, PaprikaModifiers modifier, String returnType,  PaprikaClass paprikaClass) {
        PaprikaMethod paprikaMethod = new PaprikaMethod(name, modifier, returnType, paprikaClass);
        paprikaClass.addPaprikaMethod(paprikaMethod);
        return  paprikaMethod;
    }

    public PaprikaClass getPaprikaClass() {
        return paprikaClass;
    }

    public void setPaprikaClass(PaprikaClass paprikaClass) {
        this.paprikaClass = paprikaClass;
    }

    @Override
    public String toString() {
        return this.getName() + "#" + paprikaClass;
    }

    public void useVariable(PaprikaVariable paprikaVariable) {
        usedVariables.add(paprikaVariable);
    }

    public Set<PaprikaVariable> getUsedVariables(){
        return this.usedVariables;
    }

    public void callMethod(PaprikaMethod paprikaMethod) { calledMethods.add(paprikaMethod);}

    public Set<PaprikaMethod> getCalledMethods() { return this.calledMethods; }

    public boolean haveCommonFields(PaprikaMethod paprikaMethod){
        Set<PaprikaVariable> otherVariables = paprikaMethod.getUsedVariables();
        for(PaprikaVariable paprikaVariable : usedVariables){
            if(otherVariables.contains(paprikaVariable)) return true;
        }
        return false;
    }

    public void addArgument(PaprikaArgument paprikaArgument){
        this.arguments.add(paprikaArgument);
    }

    public List<PaprikaArgument> getArguments(){
        return arguments;
    }
}
