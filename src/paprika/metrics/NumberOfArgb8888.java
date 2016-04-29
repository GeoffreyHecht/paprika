package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by antonin on 16-04-29.
 */
public class NumberOfArgb8888 extends UnaryMetric<Integer> {

    private NumberOfArgb8888(PaprikaApp paprikaApp, int nbOfBitmaps) {
        this.value = nbOfBitmaps;
        this.entity = paprikaApp;
        this.name = "number_of_bitmaps";
    }

    public static NumberOfArgb8888 createNumberOfBitmaps(PaprikaApp paprikaApp, int nbOfBitmaps) {
        NumberOfArgb8888 numberOfArgb8888 = new NumberOfArgb8888(paprikaApp, nbOfBitmaps);
        numberOfArgb8888.updateEntity();
        return numberOfArgb8888;
    }

}
