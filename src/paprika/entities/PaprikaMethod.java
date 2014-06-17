package paprika.entities;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaMethod extends Entity{
    private PaprikaClass paprikaClass;

    private PaprikaMethod(String name, PaprikaClass paprikaClass) {
        this.setName(name);
        this.paprikaClass = paprikaClass;
    }

    public static PaprikaMethod createPaprikaMethod(String name, PaprikaClass paprikaClass) {
        PaprikaMethod paprikaMethod = new PaprikaMethod(name, paprikaClass);
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
        return this.getName() + "@" + paprikaClass;
    }
}
