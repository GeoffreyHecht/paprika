package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfServices extends UnaryMetric<Integer> {

    private NumberOfServices(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "Number of Services";
    }

    public static NumberOfServices createNumberOfServices(PaprikaApp paprikaApp, int value) {
        NumberOfServices numberOfServices = new NumberOfServices(paprikaApp, value);
        numberOfServices.updateEntity();
        return numberOfServices;
    }

}
