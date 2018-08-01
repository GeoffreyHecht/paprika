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

package paprika.query.neo4j.queries;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import paprika.DatabaseManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestEngine {

    private DatabaseManager manager;
    private GraphDatabaseService service;

    public TestEngine(String dbPath) {
        manager = new DatabaseManager(dbPath);
        manager.start();
        service = manager.getGraphDatabaseService();
    }

    public List<Map<String, Object>> execute(String query) {
        try (Transaction ignored = service.beginTx()) {
            return service.execute(query).stream().collect(Collectors.toList());
        }
    }

    public void stop() {
        manager.shutDown();
    }

}
