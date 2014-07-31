package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class NumberOfImplementedInterfaces extends UnaryMetric<Integer> {

    private NumberOfImplementedInterfaces(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "number_of_implemented_interfaces";
    }

    public static NumberOfImplementedInterfaces createNumberOfImplementedInterfaces(PaprikaClass paprikaClass, int value) {
        NumberOfImplementedInterfaces numberOfImplementedInterfaces =new NumberOfImplementedInterfaces(paprikaClass, value);
        numberOfImplementedInterfaces.updateEntity();
        return numberOfImplementedInterfaces;
    }

}
