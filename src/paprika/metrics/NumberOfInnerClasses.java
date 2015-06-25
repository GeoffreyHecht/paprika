package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfInnerClasses extends UnaryMetric<Integer> {

    private NumberOfInnerClasses(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "number_of_inner_classes";
    }

    public static NumberOfInnerClasses createNumberOfInnerClasses(PaprikaApp paprikaApp, int value) {
        NumberOfInnerClasses numberOfInnerClasses = new NumberOfInnerClasses(paprikaApp, value);
        numberOfInnerClasses.updateEntity();
        return numberOfInnerClasses;
    }

}
