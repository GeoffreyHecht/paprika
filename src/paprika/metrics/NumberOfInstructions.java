package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 22/05/14.
 */
public class NumberOfInstructions extends UnaryMetric<Integer> {

    private NumberOfInstructions(PaprikaMethod paprikaMethod, int value) {
        this.value = value;
        this.entity = paprikaMethod;
        this.name = "Number of Instructions";
    }

    public static NumberOfInstructions createNumberOfInstructions(PaprikaMethod paprikaMethod, int value) {
        NumberOfInstructions  numberOfInstructions = new NumberOfInstructions(paprikaMethod, value);
        numberOfInstructions.updateEntity();
        return numberOfInstructions;
    }

}
