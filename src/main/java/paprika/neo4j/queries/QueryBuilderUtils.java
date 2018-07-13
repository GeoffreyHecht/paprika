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

package paprika.neo4j.queries;

import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.Path;
import org.neo4j.cypherdsl.expression.BooleanExpression;
import org.neo4j.cypherdsl.expression.Expression;
import org.neo4j.cypherdsl.grammar.StartNext;
import paprika.entities.PaprikaClass;
import paprika.entities.PaprikaExternalMethod;
import paprika.entities.PaprikaMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.*;
import static paprika.neo4j.RelationTypes.CALLS;

public class QueryBuilderUtils {

    public static final String ANDROID_CANVAS = "android.graphics.Canvas";

    public static StartNext getMethodCallingExternal(Identifier method, Identifier externalMethod,
                                                     String externalCall) {
        return match(node(method).label(METHOD_TYPE).out(CALLS)
                .node(externalMethod).label(EXTERNAL_METHOD_TYPE)
                .values(value(PaprikaExternalMethod.FULL_NAME, externalCall)));
    }

    public static List<Expression> getMethodResults(Identifier method, boolean details,
                                                    String countLabel) {
        return getResults(method, details, countLabel, PaprikaMethod.APP_KEY, PaprikaMethod.FULL_NAME, true);
    }

    /**
     * Same as getMethodResults, but method.app_key will not be renamed to app_key.
     */
    public static List<Expression> getAlternativeMethodResults(Identifier method, boolean details,
                                                               String countLabel) {
        return getResults(method, details, countLabel, PaprikaMethod.APP_KEY, PaprikaMethod.FULL_NAME, false);
    }

    public static List<Expression> getClassResults(Identifier aClass, boolean details,
                                                   String countLabel) {
        return getResults(aClass, details, countLabel, PaprikaClass.APP_KEY, PaprikaClass.NAME, true);
    }

    private static List<Expression> getResults(Identifier identifier, boolean details, String countLabel,
                              String appKeyProperty, String nameProperty, boolean renameAppKey) {
        List<Expression> results = new ArrayList<>();
        if (renameAppKey) {
            results.add(as(identifier.property(appKeyProperty), "app_key"));
        } else {
            results.add(identifier.property(appKeyProperty));
        }
        if (details) {
            results.add(as(identifier.property(nameProperty), "full_name"));
        } else {
            results.add(as(count(identifier), countLabel));
        }
        return results;
    }

    public static List<Expression> getCountResults(Identifier aClass, String countLabel) {
        return Arrays.asList(
                as(aClass.property(PaprikaClass.APP_KEY), "app_key"),
                as(count(aClass), countLabel));
    }

    public static Path methodCallsExternal(Identifier method, String callFullName) {
        return node(method).out(CALLS)
                .node().label(EXTERNAL_METHOD_TYPE).values(
                        value(PaprikaExternalMethod.FULL_NAME, callFullName));
    }

    public static Path getSubClassNodes(Identifier aClass, String parent) {
        return node(aClass).label(CLASS_TYPE).values(value(PaprikaClass.PARENT, parent));
    }

    public static BooleanExpression methodHasName(Identifier method, String name) {
        return method.property(PaprikaMethod.NAME).eq(name);
    }

}
