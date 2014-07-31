package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 23/05/14.
 */
public class NumberOfDeclaredLocals extends UnaryMetric<Integer> {

    private NumberOfDeclaredLocals(PaprikaMethod paprikaMethod, int value) {
        this.value = value;
        this.entity = paprikaMethod;
        this.name = "number_of_declared_locals";
    }

    public static NumberOfDeclaredLocals createNumberOfDeclaredLocals(PaprikaMethod paprikaMethod, int value) {
        NumberOfDeclaredLocals numberOfDeclaredLocals = new NumberOfDeclaredLocals(paprikaMethod, value);
        numberOfDeclaredLocals.updateEntity();
        return  numberOfDeclaredLocals;
    }

}
