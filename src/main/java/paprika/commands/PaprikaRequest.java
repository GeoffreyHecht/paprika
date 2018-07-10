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

import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.antipatterns.*;
import paprika.neo4j.queries.antipatterns.fuzzy.*;
import paprika.neo4j.queries.stats.*;

public enum PaprikaRequest {

    ARGB8888(ARGB8888Query.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new ARGB8888Query(engine);
        }
    },

    HMU(HashMapUsageQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new HashMapUsageQuery(engine);
        }
    },

    IGS(IGSQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new IGSQuery(engine);
        }
    },

    IOD(InitOnDrawQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new InitOnDrawQuery(engine);
        }
    },

    IWR(InvalidateWithoutRectQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new InvalidateWithoutRectQuery(engine);
        }
    },

    LIC(LICQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new LICQuery(engine);
        }
    },

    MIM(MIMQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new MIMQuery(engine);
        }
    },

    NLMR(NLMRQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new NLMRQuery(engine);
        }
    },

    UIO(OverdrawQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new OverdrawQuery(engine);
        }
    },

    THI(TrackingHardwareIdQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new TrackingHardwareIdQuery(engine);
        }
    },

    UHA(UHAQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new UHAQuery(engine);
        }
    },

    UCS(UnsuitedLRUCacheSizeQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new UnsuitedLRUCacheSizeQuery(engine);
        }
    },

    BLOB(BLOBQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new BLOBQuery(engine);
        }
    },

    CC(CCQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new CCQuery(engine);
        }
    },

    HAS(HeavyAsyncTaskStepsQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new HeavyAsyncTaskStepsQuery(engine);
        }
    },

    HBR(HeavyBroadcastReceiverQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new HeavyBroadcastReceiverQuery(engine);
        }
    },

    HSS(HeavyServiceStartQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new HeavyServiceStartQuery(engine);
        }
    },

    LM(LMQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new LMQuery(engine);
        }
    },

    SAK(SAKQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new SAKQuery(engine);
        }
    },

    ALL_HEAVY(AllHeavyCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new AllHeavyCommand(engine);
        }
    },

    ANALYZED(AnalyzedAppQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new AnalyzedAppQuery(engine);
        }
    },


    STATS(StatsCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new StatsCommand(engine);
        }
    },

    ALL_LCOM(PropertyQuery.ALL_LCOM) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new PropertyQuery("ALL_LCOM", engine, "Class", "lack_of_cohesion_in_methods");
        }
    },

    ALL_CYCLO(PropertyQuery.ALL_CYCLOMATIC) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new PropertyQuery("ALL_CLASS_COMPLEXITY", engine, "Class", "class_complexity");
        }
    },

    ALL_CC(PropertyQuery.ALL_CC) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new PropertyQuery("ALL_CYCLOMATIC_COMPLEXITY", engine, "Method", "cyclomatic_complexity");
        }
    },

    ALL_NUM_METHOD(PropertyQuery.ALL_METHODS) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new PropertyQuery("ALL_NUMBER_OF_METHODS", engine, "Class", "number_of_methods");
        }
    },

    COUNT_VARS(CountVariablesQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new CountVariablesQuery(engine);
        }
    },

    COUNT_INNER(CountInnerQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new CountInnerQuery(engine);
        }
    },

    COUNT_ASYNC(CountAsyncQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new CountAsyncQuery(engine);
        }
    },

    COUNT_VIEWS(CountViewsQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new CountViewsQuery(engine);
        }
    },

    NON_FUZZY(NonFuzzyCommand.KEY){
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new NonFuzzyCommand(engine);
        }
    },

    FUZZY(FuzzyCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new FuzzyCommand(engine);
        }
    },

    ALLAP(ALLAPCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new ALLAPCommand(engine);
        }
    },

    FORCE_NO_FUZZY(ForceNoFuzzyCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new ForceNoFuzzyCommand(engine);
        }
    },

    DELETE(DeleteCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new DeleteCommand(engine);
        }
    },

    DELETE_APP(DeleteAppCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new DeleteAppCommand(engine);
        }
    };

    private String key;

    PaprikaRequest(String key) {
        this.key = key;
    }

    public static PaprikaRequest getRequest(String command) {
        for (PaprikaRequest request : PaprikaRequest.values()) {
            if (request.key.equals(command)) {
                return request;
            }
        }
        return null;
    }

    public abstract PaprikaCommand getCommand(QueryEngine engine);

}
