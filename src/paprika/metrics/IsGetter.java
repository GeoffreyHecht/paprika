package paprika.metrics;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */

import paprika.entities.PaprikaMethod;

public class IsGetter extends UnaryMetric<Boolean> {

    private IsGetter(PaprikaMethod entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_getter";
    }

    public static IsGetter createIsGetter(PaprikaMethod entity, boolean value) {
        IsGetter isGetter = new IsGetter(entity, value);
        isGetter.updateEntity();
        return isGetter;
    }
}
