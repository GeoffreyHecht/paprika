package paprika.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaMethod extends Entity{
    private PaprikaClass paprikaClass;
    private Boolean isPublic;
    private Boolean isOverride;
    private String returnType;
    private Set<PaprikaVariable> usedVariables;
    private Set<PaprikaMethod> calledMethods;
    private PaprikaModifiers modifier;


    public PaprikaModifiers getModifier() {
        return modifier;
    }

    public Boolean getIsOverride() {
        return isOverride;
    }

    public void setIsOverride(Boolean isOverride) {
        this.isOverride = isOverride;
    }

    private PaprikaMethod(String name, PaprikaModifiers modifier, PaprikaClass paprikaClass) {
        this.setName(name);
        this.paprikaClass = paprikaClass;
        this.usedVariables = new HashSet<>();
        this.calledMethods = new HashSet<>();
        this.modifier = modifier;
    }

    public static PaprikaMethod createPaprikaMethod(String name, PaprikaModifiers modifier,  PaprikaClass paprikaClass) {
        PaprikaMethod paprikaMethod = new PaprikaMethod(name, modifier, paprikaClass);
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
}
