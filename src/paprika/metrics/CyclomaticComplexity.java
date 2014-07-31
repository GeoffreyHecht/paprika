package paprika.metrics;

import paprika.entities.PaprikaMethod;

/**
 * Created by Geoffrey Hecht on 22/05/14.
 */
public class CyclomaticComplexity extends UnaryMetric<Integer> {

    private CyclomaticComplexity(PaprikaMethod paprikaMethod, int value) {
        this.value = value;
        this.entity = paprikaMethod;
        this.name = "cyclomatic_complexity";
    }

    public static CyclomaticComplexity createCyclomaticComplexity(PaprikaMethod paprikaMethod, int value) {
        CyclomaticComplexity cyclomaticComplexity =  new CyclomaticComplexity(paprikaMethod, value);
        cyclomaticComplexity.updateEntity();
        paprikaMethod.getPaprikaClass().addComplexity(value);
        return cyclomaticComplexity;
    }

}
