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

import paprika.launcher.PaprikaArgParser;
import paprika.neo4j.QueryEngine;

import static paprika.launcher.Argument.DEL_KEY_ARG;
import static paprika.launcher.Argument.DEL_PACKAGE_ARG;

public class DeleteAppCommand implements PaprikaCommand {

    public static final String KEY = "DELETEAPP";

    private QueryEngine engine;

    public DeleteAppCommand(QueryEngine engine) {
        this.engine = engine;
    }

    @Override
    public void run(boolean details) {
        PaprikaArgParser arg = engine.getArgParser();
        if (arg.getArg(DEL_KEY_ARG) != null) {
            engine.deleteEntireApp(arg.getArg(DEL_KEY_ARG));
        } else {
            engine.deleteEntireAppFromPackage(arg.getArg(DEL_PACKAGE_ARG));
        }
    }

}
