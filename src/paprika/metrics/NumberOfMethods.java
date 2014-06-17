package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfMethods extends UnaryMetric<Integer> {

    private NumberOfMethods(PaprikaClass paprikaClass, int value) {
        this.setValue(value);
        this.setEntity(paprikaClass);
        this.name = "Number of Methods";
    }

    public static NumberOfMethods createNumberOfMethods(PaprikaClass paprikaClass, int value) {
        NumberOfMethods numberOfMethods = new NumberOfMethods(paprikaClass, value);
        numberOfMethods.updateEntity();
        return numberOfMethods;
    }
}
