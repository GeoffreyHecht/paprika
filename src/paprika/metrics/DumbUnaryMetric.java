package paprika.metrics;

import paprika.entities.Entity;
/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class DumbUnaryMetric<E> extends UnaryMetric<E> {
    private String description;

    private DumbUnaryMetric(Entity entity, E value, String description) {
        this.setValue(value);
        this.setEntity(entity);
        this.description = description;
    }

    public static <E> DumbUnaryMetric<E> createDumbMetric(Entity entity, E value, String description) {
        DumbUnaryMetric dumbUnaryMetric = new DumbUnaryMetric<E>(entity, value, description);
        dumbUnaryMetric.updateEntity();
        return dumbUnaryMetric;
    }

}
