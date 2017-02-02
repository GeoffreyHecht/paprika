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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public class DatabaseManager {
    private final String DB_PATH;
    private GraphDatabaseService graphDatabaseService;

    public DatabaseManager(String DB_PATH) {
        this.DB_PATH = DB_PATH;

    }

    public void start(){
        //graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        File dbFile = new File(DB_PATH);
        graphDatabaseService = new GraphDatabaseFactory().
                newEmbeddedDatabaseBuilder(dbFile).
                newGraphDatabase();
        registerShutdownHook(graphDatabaseService);
    }

    public void deleteDB(){
        shutDown();
        deleteFileOrDirectory(new File( DB_PATH));
    }

    public void shutDown()
    {
        graphDatabaseService.shutdown();
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    public GraphDatabaseService getGraphDatabaseService(){
        return graphDatabaseService;
    }

    private static void deleteFileOrDirectory( File file )
    {
        if ( file.exists() )
        {
            if ( file.isDirectory() )
            {
                for ( File child : file.listFiles() )
                {
                    deleteFileOrDirectory( child );
                }
            }
            file.delete();
        }
    }
}
