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

import paprika.analyse.metrics.classes.stat.paprika.ClassComplexity;
import paprika.analyse.metrics.classes.stat.paprika.LackOfCohesionInMethods;
import paprika.analyse.metrics.common.NumberOfMethods;
import paprika.analyse.metrics.methods.stat.CyclomaticComplexity;
import paprika.query.neo4j.QueryEngine;
import paprika.query.neo4j.queries.PaprikaQuery;
import paprika.query.neo4j.queries.QueryPropertiesReader;
import paprika.query.neo4j.queries.antipatterns.LongParameterList;
import paprika.query.neo4j.queries.antipatterns.adoctor.DebuggableRelease;
import paprika.query.neo4j.queries.antipatterns.adoctor.DurableWakelock;
import paprika.query.neo4j.queries.antipatterns.adoctor.PublicData;
import paprika.query.neo4j.queries.antipatterns.adoctor.RigidAlarmManager;
import paprika.query.neo4j.queries.antipatterns.fuzzy.*;
import paprika.query.neo4j.queries.antipatterns.memory.HashMapUsage;
import paprika.query.neo4j.queries.antipatterns.memory.LeakingInnerClass;
import paprika.query.neo4j.queries.antipatterns.memory.NoLowMemoryResolver;
import paprika.query.neo4j.queries.antipatterns.memory.UnsuitedLRUCache;
import paprika.query.neo4j.queries.antipatterns.optimization.InitOnDraw;
import paprika.query.neo4j.queries.antipatterns.optimization.InternalGetterSetter;
import paprika.query.neo4j.queries.antipatterns.optimization.MemberIgnoringMethod;
import paprika.query.neo4j.queries.antipatterns.ui.InvalidateWithoutRect;
import paprika.query.neo4j.queries.antipatterns.ui.Overdraw;
import paprika.query.neo4j.queries.antipatterns.ui.UHA;
import paprika.query.neo4j.queries.stats.*;

import java.util.Arrays;
import java.util.Collections;

import static paprika.analyse.neo4j.ModelToGraph.CLASS_TYPE;
import static paprika.analyse.neo4j.ModelToGraph.METHOD_TYPE;

/**
 * Enum for all of the available commands.
 */
public enum PaprikaRequest {

    HMU(HashMapUsage.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new HashMapUsage());
        }
    },

    IGS(InternalGetterSetter.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new InternalGetterSetter());
        }
    },

    IOD(InitOnDraw.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new InitOnDraw());
        }
    },

    IWR(InvalidateWithoutRect.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new InvalidateWithoutRect());
        }
    },

    LIC(LeakingInnerClass.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new LeakingInnerClass());
        }
    },

    MIM(MemberIgnoringMethod.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new MemberIgnoringMethod());
        }
    },

    NLMR(NoLowMemoryResolver.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new NoLowMemoryResolver());
        }
    },

    UIO(Overdraw.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new Overdraw());
        }
    },

    UHA_CMD(UHA.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new UHA());
        }
    },

    UCS(UnsuitedLRUCache.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new UnsuitedLRUCache());
        }
    },

    BLOB_CMD(BLOB.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new BLOB(engine.getPropsReader()));
        }
    },

    CC_CMD(ComplexClass.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new ComplexClass(engine.getPropsReader()));
        }
    },

    HAS(HeavyAsyncTaskSteps.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new HeavyAsyncTaskSteps(engine.getPropsReader()));
        }
    },

    HBR(HeavyBroadcastReceiver.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new HeavyBroadcastReceiver(engine.getPropsReader()));
        }
    },

    HSS(HeavyServiceStart.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new HeavyServiceStart(engine.getPropsReader()));
        }
    },

    LM(LongMethod.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new LongMethod(engine.getPropsReader()));
        }
    },

    SAK(SwissArmyKnife.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getFuzzyCommand(engine, new SwissArmyKnife(engine.getPropsReader()));
        }
    },

    DR(DebuggableRelease.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new DebuggableRelease());
        }
    },

    DW(DurableWakelock.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new DurableWakelock());
        }
    },

    PD(PublicData.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new PublicData());
        }
    },

    RAM(RigidAlarmManager.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new RigidAlarmManager());
        }
    },

    LPL(LongParameterList.KEY) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new LongParameterList());
        }
    },


    // -----------------------------------------------------------------------------------

    ALL_HEAVY("ALLHEAVY") {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return new QueriesCommand(engine, Arrays.asList(
                    new HeavyAsyncTaskSteps(engine.getPropsReader()),
                    new HeavyBroadcastReceiver(engine.getPropsReader()),
                    new HeavyServiceStart(engine.getPropsReader())
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

    ALL_CYCLO(PropertyQuery.ALL_CC) {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            return getSimpleCommand(engine, new PropertyQuery("ALL_CLASS_COMPLEXITY", CLASS_TYPE, ClassComplexity.NAME));
        }
    },

    ALL_CC(PropertyQuery.ALL_CYCLOMATIC) {
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
                    new InternalGetterSetter(), new MemberIgnoringMethod(), new LeakingInnerClass(),
                    new NoLowMemoryResolver(), new Overdraw(), new UnsuitedLRUCache(),
                    new InitOnDraw(), new UHA(), new HashMapUsage(),
                    new InvalidateWithoutRect(), new DebuggableRelease(), new DurableWakelock(),
                    new PublicData(), new RigidAlarmManager(),
                    new LongParameterList()
            ));
        }
    },

    FUZZY("FUZZY") {
        @Override
        public PaprikaCommand getCommand(QueryEngine engine) {
            QueryPropertiesReader reader = engine.getPropsReader();
            return new FuzzyCommand(engine, Arrays.asList(
                    new ComplexClass(reader), new LongMethod(reader), new SwissArmyKnife(reader),
                    new BLOB(reader), new HeavyServiceStart(reader),
                    new HeavyBroadcastReceiver(reader),
                    new HeavyAsyncTaskSteps(reader)
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
            QueryPropertiesReader reader = engine.getPropsReader();
            return new QueriesCommand(engine, Arrays.asList(
                    new ComplexClass(reader), new LongMethod(reader), new SwissArmyKnife(reader), new BLOB(reader),
                    new HeavyServiceStart(reader), new HeavyBroadcastReceiver(reader),
                    new HeavyAsyncTaskSteps(reader)
            ));
        }
    };

    private String key;

    /**
     * Constructor.
     *
     * @param key the key used to call this command in Paprika query mode
     */
    PaprikaRequest(String key) {
        this.key = key;
    }

    /**
     * Get an instance of the request matching the given name.
     *
     * @param command the name to search
     */
    public static PaprikaRequest getRequest(String command) {
        for (PaprikaRequest request : PaprikaRequest.values()) {
            if (request.key.equals(command)) {
                return request;
            }
        }
        return null;
    }

    /**
     * Creates a simple command that will run a single PaprikaQuery once executed.
     *
     * @param engine the engine to run the query
     * @param query  the query to run
     * @return the command that will execute the query on the given engine when running
     */
    public static PaprikaCommand getSimpleCommand(QueryEngine engine, PaprikaQuery query) {
        return new QueriesCommand(engine, Collections.singletonList(query));
    }

    /**
     * See {@link #getSimpleCommand(QueryEngine, PaprikaQuery)}.
     * Creates a simple command that will run a single FuzzyQuery once executed.
     */
    public static PaprikaCommand getFuzzyCommand(QueryEngine engine, FuzzyQuery query) {
        return new FuzzyCommand(engine, Collections.singletonList(query));
    }

    /**
     * Create the command matching the request.
     *
     * @param engine the engine to run the query on
     */
    public abstract PaprikaCommand getCommand(QueryEngine engine);

}
