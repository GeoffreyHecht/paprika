package paprika.metrics;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */

import paprika.entities.PaprikaClass;

public class IsInterface extends UnaryMetric<Boolean> {

    private IsInterface(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_interface";
    }

    public static IsInterface createIsInterface(PaprikaClass entity, boolean value) {
        IsInterface isInterface = new IsInterface(entity, value);
        isInterface.updateEntity();
        return isInterface;
    }
}
