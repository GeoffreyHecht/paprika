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

package paprika.query.commands;

import paprika.query.neo4j.QueryEngine;
import paprika.query.neo4j.queries.antipatterns.fuzzy.FuzzyQuery;

import java.io.IOException;
import java.util.List;

public class FuzzyCommand implements PaprikaCommand {

    private QueryEngine engine;
    private List<FuzzyQuery> queries;

    public FuzzyCommand(QueryEngine engine, List<FuzzyQuery> queries) {
        this.engine = engine;
        this.queries = queries;
    }

    @Override
    public void run(boolean details) throws IOException {
        for (FuzzyQuery query : queries) {
            engine.executeFuzzy(query, details);
        }
    }

}
