package paprika.entities;

/**
 * Created by Geoffrey Hecht on 26/06/14.
 */
public class PaprikaVariable extends Entity{
    private PaprikaClass paprikaClass;
    private String type;
    private PaprikaModifiers modifier;

    public String getType() {
        return type;
    }

    public PaprikaModifiers getModifier() {
        return modifier;
    }

    private PaprikaVariable(String name, String type, PaprikaModifiers modifier, PaprikaClass paprikaClass) {
        this.type = type;
        this.name = name;
        this.modifier = modifier;
        this.paprikaClass = paprikaClass;
    }

    public static PaprikaVariable createPaprikaVariable(String name, String type, PaprikaModifiers modifier, PaprikaClass paprikaClass) {
        PaprikaVariable paprikaVariable = new PaprikaVariable(name, type, modifier, paprikaClass);
        paprikaClass.addPaprikaVariable(paprikaVariable);
        return paprikaVariable;
    }

    public boolean isPublic(){
        return modifier == PaprikaModifiers.PUBLIC;
    }

    public boolean isPrivate(){
        return modifier == PaprikaModifiers.PRIVATE;
    }

    public boolean isProtected(){ return modifier == PaprikaModifiers.PROTECTED; }
}
