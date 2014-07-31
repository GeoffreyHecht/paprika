package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfInterfaces extends UnaryMetric<Integer> {

    private NumberOfInterfaces(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "number_of_interfaces";
    }

    public static NumberOfInterfaces createNumberOfInterfaces(PaprikaApp paprikaApp, int value) {
        NumberOfInterfaces numberOfInterfaces = new NumberOfInterfaces(paprikaApp, value);
        numberOfInterfaces.updateEntity();
        return numberOfInterfaces;
    }

}
