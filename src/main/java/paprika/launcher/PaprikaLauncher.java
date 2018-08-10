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

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import paprika.launcher.arg.PaprikaArgException;
import paprika.launcher.arg.PaprikaArgParser;

import java.io.PrintStream;

/**
 * Launches a Paprika session.
 */
public class PaprikaLauncher {

    private PaprikaArgParser argParser;
    private PrintStream out;

    /**
     * Constructor.
     *
     * @param args the args for this Paprika session
     * @param out  a PrintStream used for user feedback
     * @throws PaprikaArgException if the arguments used were invalid
     */
    public PaprikaLauncher(String[] args, PrintStream out) throws PaprikaArgException {
        this.out = out;
        this.argParser = new PaprikaArgParser();
        try {
            argParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argParser.handleError(e);
        }
    }

    public void startPaprika() {
        PaprikaStarter starter = argParser.getSelectedStarter(out);
        if (starter != null) {
            starter.start();
        }
    }

    public PaprikaArgParser getArgParser() {
        return argParser;
    }

}
