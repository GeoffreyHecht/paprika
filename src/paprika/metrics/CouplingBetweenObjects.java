package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class CouplingBetweenObjects extends UnaryMetric<Integer> {

    private CouplingBetweenObjects(PaprikaClass paprikaClass) {
        this.value = paprikaClass.getCouplingValue();
        this.entity = paprikaClass;
        this.name = "coupling_between_object_classes";
    }

    public static CouplingBetweenObjects createCouplingBetweenObjects(PaprikaClass paprikaClass) {
        CouplingBetweenObjects couplingBetweenObjects = new CouplingBetweenObjects(paprikaClass);
        couplingBetweenObjects.updateEntity();
        return couplingBetweenObjects;
    }
}
