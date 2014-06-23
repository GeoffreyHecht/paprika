package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfAttributes extends UnaryMetric<Integer> {

    private NumberOfAttributes(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "Number of Attributes";
    }

    public static NumberOfAttributes createNumberOfAttributes(PaprikaClass paprikaClass, int value) {
        NumberOfAttributes numberOfAttributes = new NumberOfAttributes(paprikaClass, value);
        numberOfAttributes.updateEntity();
        return numberOfAttributes;
    }

}
