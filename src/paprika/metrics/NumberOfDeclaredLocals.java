package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 23/05/14.
 * TODO: Verifier la pertinence de cette métrique (en Jimple tout du moins)
 */
public class NumberOfDeclaredLocals extends UnaryMetric<Integer> {

    private NumberOfDeclaredLocals(PaprikaMethod paprikaMethod, int value) {
        this.setValue(value);
        this.setEntity(paprikaMethod);
        this.name = "Number of Declared Locals";
    }

    public static NumberOfDeclaredLocals createNumberOfDeclaredLocals(PaprikaMethod paprikaMethod, int value) {
        NumberOfDeclaredLocals numberOfDeclaredLocals = new NumberOfDeclaredLocals(paprikaMethod, value);
        numberOfDeclaredLocals.updateEntity();
        return  numberOfDeclaredLocals;
    }

}
