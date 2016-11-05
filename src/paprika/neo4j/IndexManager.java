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

package paprika.neo4j;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

/**
 * Created by Geoffrey Hecht on 12/01/15.
 */
public class IndexManager {
    private GraphDatabaseService graphDatabaseService;

    public IndexManager(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    public void createIndex(){
        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            Schema schema = graphDatabaseService.schema();
            if(schema.getIndexes(DynamicLabel.label("Variable")).iterator().hasNext()) {
                     schema.indexFor(DynamicLabel.label("Variable"))
                    .on("app_key")
                    .create();
             }
            if(schema.getIndexes(DynamicLabel.label("Method")).iterator().hasNext()) {
                schema.indexFor(DynamicLabel.label("Method"))
                        .on("app_key")
                        .create();
                schema.indexFor(DynamicLabel.label("Method"))
                        .on("is_static")
                        .create();
            }
            if(schema.getIndexes(DynamicLabel.label("Argument")).iterator().hasNext()) {
                schema.indexFor(DynamicLabel.label("Argument"))
                        .on("app_key")
                        .create();
                schema.indexFor(DynamicLabel.label("Argument"))
                        .on("app_key")
                        .create();
            }
            if(schema.getIndexes(DynamicLabel.label("ExternalClass")).iterator().hasNext()) {
                schema.indexFor(DynamicLabel.label("ExternalClass"))
                        .on("app_key")
                        .create();
            }
            if(schema.getIndexes(DynamicLabel.label("ExternalMethod")).iterator().hasNext()) {
                schema.indexFor(DynamicLabel.label("ExternalMethod"))
                        .on("app_key")
                        .create();
            }
            tx.success();
        }
        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            org.neo4j.graphdb.index.IndexManager index = graphDatabaseService.index();
            if(!index.existsForRelationships("calls")) {
                index.forRelationships("calls");
            }
            tx.success();
        }
    }
}
