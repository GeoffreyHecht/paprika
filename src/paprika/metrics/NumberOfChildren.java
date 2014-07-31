package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfChildren extends UnaryMetric<Integer> {

    private NumberOfChildren(PaprikaClass paprikaClass) {
        this.value = paprikaClass.getChildren();
        this.entity = paprikaClass;
        this.name = "number_of_children";
    }

    public static NumberOfChildren createNumberOfChildren(PaprikaClass paprikaClass) {
        NumberOfChildren numberOfChildren = new NumberOfChildren(paprikaClass);
        numberOfChildren.updateEntity();
        return numberOfChildren;
    }
}
