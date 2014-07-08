package paprika.entities;

/**
 * Created by Geoffrey Hecht on 26/06/14.
 */
public class PaprikaField extends Entity{
    private PaprikaClass paprikaClass;
    private String type;
    private Modifiers modifier;

    public enum Modifiers {
        PRIVATE,
        PROTECTED,
        PUBLIC
    }

    private PaprikaField(String name, String type, Modifiers modifier, PaprikaClass paprikaClass) {
        this.type = type;
        this.name = name;
        this.paprikaClass = paprikaClass;
    }

    public static PaprikaField createPaprikaField(String name, String type, Modifiers modifier, PaprikaClass paprikaClass) {
        PaprikaField paprikaField = new PaprikaField(name, type, modifier, paprikaClass);
        paprikaClass.addPaprikaField(paprikaField);
        return  paprikaField;
    }

    public boolean isPublic(){
        return modifier == Modifiers.PUBLIC;
    }

    public boolean isPrivate(){
        return modifier == Modifiers.PRIVATE;
    }

    public boolean isProtected(){
        return modifier == Modifiers.PROTECTED;
    }
}
