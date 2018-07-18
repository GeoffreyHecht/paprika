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

import paprika.metrics.classes.stat.paprika.ClassComplexity;
import paprika.metrics.classes.stat.paprika.LackOfCohesionInMethods;
import paprika.metrics.common.NumberOfMethods;
import paprika.metrics.methods.stat.CyclomaticComplexity;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;
import paprika.neo4j.queries.antipatterns.*;
import paprika.neo4j.queries.antipatterns.fuzzy.*;
import paprika.neo4j.queries.stats.*;

import java.util.Arrays;
import java.util.Collections;

import static paprika.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.neo4j.ModelToGraph.METHOD_TYPE;

public enum PaprikaRequest {

    HMU(HashMapUsageQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new HashMapUsageQuery());
        }
    },

    IGS(IGSQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new IGSQuery());
        }
    },

    IOD(InitOnDrawQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new InitOnDrawQuery());
        }
    },

    IWR(InvalidateWithoutRectQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new InvalidateWithoutRectQuery());
        }
    },

    LIC(LICQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new LICQuery());
        }
    },

    MIM(MIMQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new MIMQuery());
        }
    },

    NLMR(NLMRQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new NLMRQuery());
        }
    },

    UIO(OverdrawQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new OverdrawQuery());
        }
    },

    UHA(UHAQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new UHAQuery());
        }
    },

    UCS(UnsuitedLRUCacheSizeQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new UnsuitedLRUCacheSizeQuery());
        }
    },

    BLOB(BLOBQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new BLOBQuery());
        }
    },

    CC(CCQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new CCQuery());
        }
    },

    HAS(HeavyAsyncTaskStepsQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new HeavyAsyncTaskStepsQuery());
        }
    },

    HBR(HeavyBroadcastReceiverQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new HeavyBroadcastReceiverQuery());
        }
    },

    HSS(HeavyServiceStartQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new HeavyServiceStartQuery());
        }
    },

    LM(LMQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new LMQuery());
        }
    },

    SAK(SAKQuery.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new SAKQuery());
        }
    },

    ALL_HEAVY("ALLHEAVY") {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new QueriesCommand(engine, Arrays.asList(
                    new HeavyAsyncTaskStepsQuery(),
                    new HeavyBroadcastReceiverQuery(),
                    new HeavyServiceStartQuery()
            ));
        }
    },

    ANALYZED(AnalyzedAppQuery.COMMAND_KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new AnalyzedAppQuery());
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
            return getSimpleCommand(engine, new PropertyQuery("ALL_LCOM", CLASS_TYPE, LackOfCohesionInMethods.NAME));
        }
    },

    ALL_CYCLO(PropertyQuery.ALL_CYCLOMATIC) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new PropertyQuery("ALL_CLASS_COMPLEXITY", CLASS_TYPE, ClassComplexity.NAME));
        }
    },

    ALL_CC(PropertyQuery.ALL_CC) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new PropertyQuery("ALL_CYCLOMATIC_COMPLEXITY", METHOD_TYPE, CyclomaticComplexity.NAME));
        }
    },

    ALL_NUM_METHOD(PropertyQuery.ALL_METHODS) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new PropertyQuery("ALL_NUMBER_OF_METHODS", CLASS_TYPE, NumberOfMethods.NAME));
        }
    },

    COUNT_VARS(CountVariablesQuery.COMMAND_KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new CountVariablesQuery());
        }
    },

    COUNT_INNER(CountInnerQuery.COMMAND_KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new CountInnerQuery());
        }
    },

    COUNT_ASYNC(CountAsyncQuery.COMMAND_KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new CountAsyncQuery());
        }
    },

    COUNT_VIEWS(CountViewsQuery.COMMAND_KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new CountViewsQuery());
        }
    },

    NON_FUZZY("NONFUZZY") {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new QueriesCommand(engine, Arrays.asList(
                    new IGSQuery(), new MIMQuery(), new LICQuery(), new NLMRQuery(),
                    new OverdrawQuery(), new UnsuitedLRUCacheSizeQuery(),
                    new InitOnDrawQuery(), new UHAQuery(), new HashMapUsageQuery(),
                    new InvalidateWithoutRectQuery()));
        }
    },

    FUZZY("FUZZY") {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new FuzzyCommand(engine, Arrays.asList(
                    new CCQuery(), new LMQuery(), new SAKQuery(),
                    new BLOBQuery(), new HeavyServiceStartQuery(),
                    new HeavyBroadcastReceiverQuery(),
                    new HeavyAsyncTaskStepsQuery()
            ));
        }
    },

    ALLAP(ALLAPCommand.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new ALLAPCommand(engine, FUZZY, NON_FUZZY);
        }
    },

    FORCE_NO_FUZZY("FORCENOFUZZY") {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new QueriesCommand(engine, Arrays.asList(
                    new CCQuery(), new LMQuery(), new SAKQuery(), new BLOBQuery(),
                    new HeavyServiceStartQuery(), new HeavyBroadcastReceiverQuery(),
                    new HeavyAsyncTaskStepsQuery()
            ));
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

    public static PaprikaCommand getSimpleCommand(QueryEngine engine, PaprikaQuery query) {
        return new QueriesCommand(engine, Collections.singletonList(query));
    }

    public static PaprikaCommand getFuzzyCommand(QueryEngine engine, FuzzyQuery query) {
        return new FuzzyCommand(engine, Collections.singletonList(query));
    }

    public abstract PaprikaCommand getCommand(QueryEngine engine);

}
