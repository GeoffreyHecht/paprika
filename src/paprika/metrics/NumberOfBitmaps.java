package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by antonin on 16-04-29.
 */
public class NumberOfBitmaps extends UnaryMetric<Integer> {

    private NumberOfBitmaps(PaprikaApp paprikaApp, int nbOfBitmaps) {
        this.value = nbOfBitmaps;
        this.entity = paprikaApp;
        this.name = "number_of_bitmaps";
    }

    public static NumberOfBitmaps createNumberOfBitmaps(PaprikaApp paprikaApp, int nbOfBitmaps) {
        NumberOfBitmaps numberOfBitmaps = new NumberOfBitmaps(paprikaApp, nbOfBitmaps);
        numberOfBitmaps.updateEntity();
        return numberOfBitmaps;
    }

}
