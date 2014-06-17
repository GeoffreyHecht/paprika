package paprika.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaApp extends Entity{
    public List<PaprikaClass> getPaprikaClasses() {
        return paprikaClasses;
    }

    private List<PaprikaClass> paprikaClasses;

    private PaprikaApp(String name) {
        this.setName(name);
        this.paprikaClasses = new ArrayList<PaprikaClass>();
    }

    public static PaprikaApp createPaprikaApp(String name) {
        return new PaprikaApp(name);
    }

    public void addPaprikaClass(PaprikaClass paprikaClass){
        paprikaClasses.add(paprikaClass);
    }
}
