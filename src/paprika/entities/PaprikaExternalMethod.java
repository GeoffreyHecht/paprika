package paprika.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaExternalMethod extends Entity{
    private PaprikaExternalClass paprikaExternalClass;
    private List<PaprikaExternalArgument> paprikaExternalArguments;
    private String returnType;

    public String getReturnType() {
        return returnType;
    }

    public List<PaprikaExternalArgument> getPaprikaExternalArguments() {
        return paprikaExternalArguments;
    }

    private PaprikaExternalMethod(String name, String returnType, PaprikaExternalClass paprikaExternalClass) {
        this.setName(name);
        this.paprikaExternalClass = paprikaExternalClass;
        this.returnType = returnType;
        this.paprikaExternalArguments = new ArrayList<>();
    }

    public static PaprikaExternalMethod createPaprikaExternalMethod(String name, String returnType,  PaprikaExternalClass paprikaClass) {
        PaprikaExternalMethod paprikaMethod = new PaprikaExternalMethod(name, returnType, paprikaClass);
        paprikaClass.addPaprikaExternalMethod(paprikaMethod);
        return  paprikaMethod;
    }

    public PaprikaExternalClass getPaprikaExternalClass() {
        return paprikaExternalClass;
    }

    public void setPaprikaExternalClass(PaprikaExternalClass paprikaClass) {
        this.paprikaExternalClass = paprikaClass;
    }

    @Override
    public String toString() {
        return this.getName() + "#" + paprikaExternalClass;
    }

    public void addExternalArgument(PaprikaExternalArgument paprikaExternalArgument) {
        this.paprikaExternalArguments.add(paprikaExternalArgument);
    }
}
