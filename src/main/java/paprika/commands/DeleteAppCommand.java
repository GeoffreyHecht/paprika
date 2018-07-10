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

import net.sourceforge.argparse4j.inf.Namespace;
import paprika.neo4j.QueryEngine;

public class DeleteAppCommand implements PaprikaCommand {

    public static final String KEY = "DELETEAPP";

    private QueryEngine engine;

    public DeleteAppCommand(QueryEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run(boolean details) {
        Namespace arg = engine.getArg();
        if (arg.get("delKey") != null) {
            engine.deleteEntireApp(arg.getString("delKey"));
        } else {
            engine.deleteEntireAppFromPackage(arg.getString("delPackage"));
        }
    }

}
