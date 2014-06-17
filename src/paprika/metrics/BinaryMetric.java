package paprika.metrics;

import paprika.entities.Entity;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public abstract class BinaryMetric<E> extends Metric{
    private Entity source;
    private Entity target;

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public Entity getSource() {
        return source;
    }

    public void setSource(Entity source) {
        this.source = source;
    }
}
