package paprika.metrics;

import paprika.entities.PaprikaClass;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 * LCOM2 (Chidamber et Kemerer 1991)
 */
public class LackofCohesionInMethods extends UnaryMetric<Integer> {

    private LackofCohesionInMethods(PaprikaClass paprikaClass) {
        this.value = paprikaClass.computeLCOM();
        this.entity = paprikaClass;
        this.name = "Lack of Cohesion in Methods";
    }

    public static LackofCohesionInMethods createLackofCohesionInMethods(PaprikaClass paprikaClass) {
        LackofCohesionInMethods couplingBetweenObjects = new LackofCohesionInMethods(paprikaClass);
        couplingBetweenObjects.updateEntity();
        return couplingBetweenObjects;
    }
}
