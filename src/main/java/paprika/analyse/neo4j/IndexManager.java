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

package paprika.analyse.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;
import paprika.analyse.entities.*;
import paprika.analyse.metrics.common.IsStatic;

import static paprika.analyse.neo4j.ModelToGraph.*;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class IndexManager {

    private GraphDatabaseService graphDatabaseService;

    public IndexManager(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    public void createIndex() {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Schema schema = graphDatabaseService.schema();
            if (schema.getIndexes(VARIABLE_LABEL).iterator().hasNext()) {
                schema.indexFor(VARIABLE_LABEL)
                        .on(PaprikaVariable.N4J_APP_KEY)
                        .create();
            }
            if (schema.getIndexes(METHOD_LABEL).iterator().hasNext()) {
                schema.indexFor(METHOD_LABEL)
                        .on(PaprikaMethod.N4J_APP_KEY)
                        .create();
                schema.indexFor(METHOD_LABEL)
                        .on(IsStatic.NAME)
                        .create();
            }
            if (schema.getIndexes(ARGUMENT_LABEL).iterator().hasNext()) {
                schema.indexFor(ARGUMENT_LABEL)
                        .on(PaprikaArgument.N4J_APP_KEY)
                        .create();
            }
            if (schema.getIndexes(EXTERNAL_CLASS_LABEL).iterator().hasNext()) {
                schema.indexFor(EXTERNAL_CLASS_LABEL)
                        .on(PaprikaExternalClass.N4J_APP_KEY)
                        .create();
            }
            if (schema.getIndexes(EXTERNAL_METHOD_LABEL).iterator().hasNext()) {
                schema.indexFor(EXTERNAL_METHOD_LABEL)
                        .on(PaprikaExternalMethod.N4J_APP_KEY)
                        .create();
            }
            tx.success();
        }
        try (Transaction tx = graphDatabaseService.beginTx()) {
            org.neo4j.graphdb.index.IndexManager index = graphDatabaseService.index();
            if (!index.existsForRelationships(RelationTypes.CALLS.name())) {
                index.forRelationships(RelationTypes.CALLS.name());
            }
            tx.success();
        }
    }
}
