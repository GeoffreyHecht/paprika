/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprika.commands;

import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.antipatterns.fuzzy.*;

import java.io.IOException;

public class FuzzyCommand implements PaprikaCommand {

    public static final String KEY = "FUZZY";

    private QueryEngine engine;

    public FuzzyCommand(QueryEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run(boolean details) throws IOException {
        new CCQuery(engine).executeFuzzy(details);
        new LMQuery(engine).executeFuzzy(details);
        new SAKQuery(engine).executeFuzzy(details);
        new BLOBQuery(engine).executeFuzzy(details);
        new HeavyServiceStartQuery(engine).executeFuzzy(details);
        new HeavyBroadcastReceiverQuery(engine).executeFuzzy(details);
        new HeavyAsyncTaskStepsQuery(engine).executeFuzzy(details);
    }

}
