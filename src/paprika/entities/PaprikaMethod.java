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
    private Set<PaprikaField> usedFields;

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsOverride() {
        return isOverride;
    }

    public void setIsOverride(Boolean isOverride) {
        this.isOverride = isOverride;
    }

    private PaprikaMethod(String name, PaprikaClass paprikaClass) {
        this.setName(name);
        this.paprikaClass = paprikaClass;
        this.usedFields = new HashSet<>();
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
        return this.getName() + "#" + paprikaClass;
    }

    public void useField(PaprikaField paprikaField) {
        usedFields.add(paprikaField);
    }


    public Set<PaprikaField> getUsedFields(){
        return this.usedFields;
    }

    public boolean haveCommonFields(PaprikaMethod paprikaMethod){
        Set<PaprikaField> otherFields = paprikaMethod.getUsedFields();
        for(PaprikaField paprikaField : usedFields){
            if(otherFields.contains(paprikaField)) return true;
        }
        return false;
    }
}
