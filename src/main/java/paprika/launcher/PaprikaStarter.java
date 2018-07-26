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

package paprika.launcher;

import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.neo4j.QueryEngine;
import paprika.query.neo4j.queries.QueryPropertiesException;
import paprika.query.neo4j.queries.QueryPropertiesReader;

import java.io.IOException;
import java.io.PrintStream;

import static paprika.launcher.arg.Argument.THRESHOLDS_ARG;

public abstract class PaprikaStarter {

    protected PaprikaArgParser argParser;
    protected PrintStream out;

    public PaprikaStarter(PaprikaArgParser argParser, PrintStream out) {
        this.argParser = argParser;
        this.out = out;
    }

    public abstract void start();

    protected QueryEngine createQueryEngine() throws IOException, QueryPropertiesException {
        QueryPropertiesReader reader = new QueryPropertiesReader();
        reader.loadProperties(argParser.getArg(THRESHOLDS_ARG));
        return new QueryEngine(argParser, reader);
    }

}
