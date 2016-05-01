package paprika.metrics;

import paprika.entities.PaprikaExternalArgument;

/**
 * Created by antonin on 16-04-29.
 */
public class IsARGB8888 extends UnaryMetric<Boolean> {

    private IsARGB8888(PaprikaExternalArgument paprikaExternalArgument, boolean value) {
        this.value = value;
        this.entity = paprikaExternalArgument;
        this.name = "is_argb_8888";
    }

    public static IsARGB8888 createIsARGB8888(PaprikaExternalArgument paprikaExternalArgument, boolean value) {
        IsARGB8888 isARGB8888 = new IsARGB8888(paprikaExternalArgument, value);
        isARGB8888.updateEntity();
        return isARGB8888;
    }

}
