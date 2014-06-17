package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfInterfaces extends UnaryMetric<Integer> {

    private NumberOfInterfaces(PaprikaApp paprikaApp, int value) {
        this.setValue(value);
        this.setEntity(paprikaApp);
        this.name = "Number of Interfaces";
    }

    public static NumberOfInterfaces createNumberOfInterfaces(PaprikaApp paprikaApp, int value) {
        NumberOfInterfaces numberOfInterfaces = new NumberOfInterfaces(paprikaApp, value);
        numberOfInterfaces.updateEntity();
        return numberOfInterfaces;
    }

}
