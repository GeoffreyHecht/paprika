package paprika.entities;

/**
 * Created by Geoffrey Hecht on 13/08/14.
 */
public class PaprikaExternalArgument extends Entity{
    private PaprikaExternalMethod paprikaExternalMethod;
    private int position;

    private PaprikaExternalArgument(String name, int position, PaprikaExternalMethod paprikaExternalMethod) {
        this.paprikaExternalMethod = paprikaExternalMethod;
        this.name = name;
        this.position = position;
    }

    public static PaprikaExternalArgument createPaprikaExternalArgument(String name, int position,PaprikaExternalMethod paprikaExternalMethod){
        PaprikaExternalArgument paprikaExternalArgument = new PaprikaExternalArgument(name,position,paprikaExternalMethod);
        paprikaExternalMethod.addExternalArgument(paprikaExternalArgument);
        return paprikaExternalArgument;
    }

    public int getPosition() {
        return position;
    }
}
