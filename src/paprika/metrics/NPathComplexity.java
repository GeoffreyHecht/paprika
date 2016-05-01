package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Antonin Carette on 16-04-29.
 */
public class NPathComplexity extends UnaryMetric<Integer> {

    private NPathComplexity(PaprikaClass paprikaClass) {
        this.value = paprikaClass.computeNPathComplexity();
        this.entity = paprikaClass;
        this.name = "npath_complexity";
    }

    public static NPathComplexity createNPathComplexity(PaprikaClass paprikaClass) {
        NPathComplexity npath_complexity = new NPathComplexity(paprikaClass);
        npath_complexity.updateEntity();
        return npath_complexity;
    }

}
