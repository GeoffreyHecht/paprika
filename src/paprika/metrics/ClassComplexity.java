package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 22/05/14.
 */
public class ClassComplexity extends UnaryMetric<Integer> {

    private ClassComplexity(PaprikaClass paprikaClass) {
        this.setValue(paprikaClass.getComplexity());
        this.setEntity(paprikaClass);
        this.name = "Class Complexity";
    }

    public static ClassComplexity createClassComplexity(PaprikaClass paprikaClass) {
        ClassComplexity classComplexity =  new ClassComplexity(paprikaClass);
        classComplexity.updateEntity();
        return classComplexity;
    }

}
