package paprika.metrics;

import paprika.entities.PaprikaApp;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class NumberOfAsyncTasks extends UnaryMetric<Integer> {

    private NumberOfAsyncTasks(PaprikaApp paprikaApp, int value) {
        this.value = value;
        this.entity = paprikaApp;
        this.name = "number_of_async_tasks";
    }

    public static NumberOfAsyncTasks createNumberOfAsyncTasks(PaprikaApp paprikaApp, int value) {
        NumberOfAsyncTasks numberOfAsyncTasks = new NumberOfAsyncTasks(paprikaApp, value);
        numberOfAsyncTasks.updateEntity();
        return numberOfAsyncTasks;
    }

}
