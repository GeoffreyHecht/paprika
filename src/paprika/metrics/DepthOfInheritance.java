package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 21/05/14.
 */
public class DepthOfInheritance extends UnaryMetric<Integer> {
    private DepthOfInheritance(PaprikaClass paprikaClass, int value) {
        this.value = value;
        this.entity = paprikaClass;
        this.name = "depth_of_inheritance";
    }

    public static DepthOfInheritance createDepthOfInheritance(PaprikaClass paprikaClass, int value) {
        DepthOfInheritance depthOfInheritance = new DepthOfInheritance(paprikaClass, value);
        depthOfInheritance.updateEntity();
        return depthOfInheritance;
    }

}
