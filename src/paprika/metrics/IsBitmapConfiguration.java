package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by antonin on 16-04-29.
 */
public class IsBitmapConfiguration extends UnaryMetric<Boolean> {

    private IsBitmapConfiguration(PaprikaClass paprikaClass, boolean value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "is_bitmap_config";
    }

    public static IsBitmapConfiguration createIsBitmapConfiguration(PaprikaClass paprikaClass, boolean value) {
        IsBitmapConfiguration isBitmapConfig = new IsBitmapConfiguration(paprikaClass, value);
        isBitmapConfig.updateEntity();
        return isBitmapConfig;
    }

}
