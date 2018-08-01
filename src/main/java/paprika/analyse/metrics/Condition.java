package paprika.analyse.metrics;

import paprika.analyse.entities.Entity;

public abstract class Condition<T, E extends Entity> {

    private String metricName;

    public Condition(String metricName) {
        this.metricName = metricName;
    }

    public abstract boolean matches(T item);

    public boolean createIfMatching(T item, E entity) {
        if (matches(item)) {
            UnaryMetric<Boolean> metric = new UnaryMetric<>(metricName, entity, true);
            metric.updateEntity();
            return true;
        }
        return false;
    }


}
