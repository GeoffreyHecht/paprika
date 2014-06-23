package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class NumberOfAbstractClasses extends UnaryMetric<Integer> {

    private NumberOfAbstractClasses(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "Number of Abstract Classes";
    }

    public static NumberOfAbstractClasses createNumberOfAbstractClasses(PaprikaApp paprikaApp, int value) {
        NumberOfAbstractClasses numberOfAbstractClasses = new NumberOfAbstractClasses(paprikaApp, value);
        numberOfAbstractClasses.updateEntity();
        return numberOfAbstractClasses;
    }

}
