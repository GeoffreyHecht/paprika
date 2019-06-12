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

import java.io.IOException;

public class CustomCommand implements PaprikaCommand {

    private String request;
    private QueryEngine engine;

    public CustomCommand(QueryEngine engine, String request) {
        this.request = request;
        this.engine = engine;
    }

    @Override
    public void run(boolean details) throws IOException {
        engine.executeAndWriteToCSV(request, "_CUSTOM.csv");
    }

}
