package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfActivities extends UnaryMetric<Integer> {

    private NumberOfActivities(PaprikaApp paprikaApp, int value) {
        this.setValue(value);
        this.setEntity(paprikaApp);
        this.name = "Number of Activities";
    }

    public static NumberOfActivities createNumberOfActivities(PaprikaApp paprikaApp, int value) {
        NumberOfActivities numberOfActivities =  new NumberOfActivities(paprikaApp, value);
        numberOfActivities.updateEntity();
        return numberOfActivities;
    }

}
