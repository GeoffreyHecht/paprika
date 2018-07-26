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

    ALL("all", "all-help") {
        @Override
        public PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out) {
            throw new RuntimeException(this.name + " is not a valid Paprika argument");
        }

        @Override
        public void setupAllArgs(Subparsers subparsers) {
            // Do nothing
        }

    };

    protected String name;
    private String help;
    private Subparser subparser;

    PaprikaMode(String name, String help) {
        this.name = name;
        this.help = help;
    }

    public abstract PaprikaStarter getStarter(PaprikaArgParser parser, PrintStream out);

    public void setupAllArgs(Subparsers subparsers) {
        subparser = subparsers.addParser(name).help(help);
        for (Argument arg : Argument.getAllArguments(this)) {
            arg.setup(subparser);
        }
    }

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
