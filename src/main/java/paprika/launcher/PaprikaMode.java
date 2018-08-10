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

import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import paprika.DeleteModeStarter;
import paprika.analyse.AnalyseModeStarter;
import paprika.launcher.arg.Argument;
import paprika.launcher.arg.PaprikaArgParser;
import paprika.query.QueryModeStarter;

import java.io.PrintStream;

/**
 * Enum for the various Paprika execution modes.
 */
public enum PaprikaMode {

    ANALYSE_MODE("analyse", "Analyse an app") {
        @Override
        public PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out) {
            return new AnalyseModeStarter(parser, out);
        }
    },

    QUERY_MODE("query", "Query the database") {
        @Override
        public PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out) {
            return new QueryModeStarter(parser, out);
        }
    },

    DELETE_MODE("delete", "Delete apps from the database") {
        @Override
        public PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out) {
            return new DeleteModeStarter(parser, out);
        }
    },

    /**
     * Shortcut used to avoid repeating argument that belong to all Paprika modes,
     * such as -db.
     */
    ALL("all", "all-help") {
        @Override
        public PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out) {
            throw new UnsupportedOperationException(this.name + " is not a valid Paprika argument");
        }

        @Override
        public void setupAllArgs(Subparsers subparsers) {
            // Do nothing
        }

    };

    protected String name;
    private String help;
    private Subparser subparser;

    /**
     * Constructor.
     *
     * @param name the name of the Paprika mode, used as an arg
     * @param help the help message describing what this mode does
     */
    PaprikaMode(String name, String help) {
        this.name = name;
        this.help = help;
    }

    /**
     * Returns an instance of the main execution class for the mode.
     *
     * @param parser the parser of the Paprika arguments
     * @param out    a PrintStream used for user feedback
     */
    public abstract PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out);

    /**
     * Sets up all the args able to be parsed by Paprika.
     *
     * @param subparsers the subparsers to insert the arguments into
     */
    public void setupAllArgs(Subparsers subparsers) {
        subparser = subparsers.addParser(name).help(help);
        for (Argument arg : Argument.getAllArguments(this)) {
            arg.setup(subparser);
        }
    }

    /**
     * Returns the execution mode whose name matches the given String.
     *
     * @param modeName the name of the mode
     */
    public static PaprikaMode getMode(String modeName) {
        for (PaprikaMode mode : values()) {
            if (mode.name.equals(modeName)) {
                return mode;
            }
        }
        return null;
    }

    public Subparser getSubparser() {
        return subparser;
    }

    @Override
    public String toString() {
        return name;
    }

}
