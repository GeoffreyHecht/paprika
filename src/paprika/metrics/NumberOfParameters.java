package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class NumberOfParameters extends UnaryMetric<Integer> {

    private NumberOfParameters(PaprikaMethod paprikaMethod, int value) {
        this.value = value;
        this.entity = paprikaMethod;
        this.name = "Number of Parameters";
    }

    public static NumberOfParameters createNumberOfParameters(PaprikaMethod paprikaMethod, int value) {
        NumberOfParameters numberOfParameters = new NumberOfParameters(paprikaMethod, value);
        numberOfParameters.updateEntity();
        return numberOfParameters;
    }

}
