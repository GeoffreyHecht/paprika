package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 22/05/14.
 */
public class NumberOfCallers extends UnaryMetric<Integer> {

    private NumberOfCallers(PaprikaMethod paprikaMethod, int value) {
        this.setValue(value);
        this.setEntity(paprikaMethod);
        this.name = "Number of Callers";
    }

    public static NumberOfCallers createNumberOfCallers(PaprikaMethod paprikaMethod, int value) {
        NumberOfCallers numberOfCallers = new NumberOfCallers(paprikaMethod, value);
        numberOfCallers.updateEntity();
        return numberOfCallers;
    }
}
