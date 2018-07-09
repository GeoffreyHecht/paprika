package paprika.metrics;

import paprika.entities.Entity;

public abstract class Condition<T, E extends Entity> {

    private String metricName;

    public Condition(String metricName) {
        this.metricName = metricName;
    }

    public abstract boolean matches(T sootItem);

    public boolean createIfMatching(T sootItem, E entity) {
        if (matches(sootItem)) {
            UnaryMetric<Boolean> metric = new UnaryMetric<>(metricName, entity, true);
            metric.updateEntity();
            return true;
        }
        return false;
    }


}
