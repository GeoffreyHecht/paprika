package paprika.metrics;

/**
 * Created by Geoffrey Hecht on 06/08/14.
 */

import paprika.entities.PaprikaClass;

public class IsContentProvider extends UnaryMetric<Boolean> {

    private IsContentProvider(PaprikaClass entity, boolean value) {
        this.value = value;
        this.entity = entity;
        this.name = "is_content_provider";
    }

    public static IsContentProvider createIsContentProvider(PaprikaClass entity, boolean value) {
        IsContentProvider isContentProvider= new IsContentProvider(entity, value);
        isContentProvider.updateEntity();
        return isContentProvider;
    }
}
