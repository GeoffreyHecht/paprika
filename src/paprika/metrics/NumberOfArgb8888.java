package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by antonin on 16-04-29.
 */
public class NumberOfArgb8888 extends UnaryMetric<Integer> {

    private NumberOfArgb8888(PaprikaApp paprikaApp, int nbOfArgb8888) {
        this.value = nbOfArgb8888;
        this.entity = paprikaApp;
        this.name = "number_of_argb_8888";
    }

    public static NumberOfArgb8888 createNumberOfArgb8888(PaprikaApp paprikaApp, int nbOfArgb8888) {
        NumberOfArgb8888 numberOfArgb8888 = new NumberOfArgb8888(paprikaApp, nbOfArgb8888);
        numberOfArgb8888.updateEntity();
        return numberOfArgb8888;
    }

}
