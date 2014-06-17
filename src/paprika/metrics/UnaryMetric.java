package paprika.metrics;

import paprika.entities.Entity;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public abstract class UnaryMetric<E> extends Metric{
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    protected void setEntity(Entity entity) {
        this.entity = entity;
    }

    public String toString() {
        return this.getEntity() + " " + this.getName() + " : "+ this.getValue();
    }

    protected void updateEntity(){
        entity.addMetric(this);
    }
}
