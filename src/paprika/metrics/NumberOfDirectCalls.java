package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 22/05/14.
 */
public class NumberOfDirectCalls extends UnaryMetric<Integer> {

    private NumberOfDirectCalls(PaprikaMethod paprikaMethod, int value) {
        this.setValue(value);
        this.setEntity(paprikaMethod);
        this.name = "Number of Direct Calls";
    }

    public static NumberOfDirectCalls createNumberOfDirectCalls(PaprikaMethod paprikaMethod, int value) {
        NumberOfDirectCalls numberOfDirectCalls = new NumberOfDirectCalls(paprikaMethod, value);
        numberOfDirectCalls.updateEntity();
        return numberOfDirectCalls;
    }
}
