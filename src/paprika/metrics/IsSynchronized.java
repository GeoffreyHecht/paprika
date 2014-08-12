package paprika.metrics;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */

import paprika.entities.PaprikaMethod;

public class IsSynchronized extends UnaryMetric<Boolean> {

    private IsSynchronized(PaprikaMethod entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_synchronized";
    }

    public static IsSynchronized createIsSynchronized(PaprikaMethod entity, boolean value) {
        IsSynchronized isSynchronized = new IsSynchronized(entity, value);
        isSynchronized.updateEntity();
        return isSynchronized;
    }
}
