package paprika.entities;

/**
 * Created by Geoffrey Hecht on 13/08/14.
 */
public class PaprikaArgument extends Entity{
    private PaprikaMethod paprikaMethod;
    private int position;

    private PaprikaArgument(String name, int position,PaprikaMethod paprikaMethod) {
        this.paprikaMethod = paprikaMethod;
        this.name = name;
        this.position = position;
    }

    public static PaprikaArgument createPaprikaArgument(String name, int position,PaprikaMethod paprikaMethod){
        PaprikaArgument paprikaArgument = new PaprikaArgument(name,position,paprikaMethod);
        paprikaMethod.addArgument(paprikaArgument);
        return paprikaArgument;
    }

    public int getPosition() {
        return position;
    }
}
