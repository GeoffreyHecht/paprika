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

package paprika.neo4j.queries.antipatterns;

import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.expression.BooleanExpression;
import paprika.entities.PaprikaExternalMethod;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import java.util.Arrays;
import java.util.List;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.EXTERNAL_METHOD_TYPE;
import static paprika.neo4j.ModelToGraph.METHOD_TYPE;
import static paprika.neo4j.RelationTypes.CALLS;
import static paprika.neo4j.queries.QueryBuilderUtils.ANDROID_CANVAS;
import static paprika.neo4j.queries.QueryBuilderUtils.getAlternativeMethodResults;

/**
 * Created by Geoffrey Hecht on 18/08/15.
 */
public class UHAQuery extends PaprikaQuery {

    public static final String KEY = "UHA";

    private static final String ANDROID_PAINT = "android.graphics.Paint";

    private static final List<String> UNSUPPORTED_OPS = Arrays.asList(
            formatMethod("drawPicture", ANDROID_CANVAS),
            formatMethod("drawVertices", ANDROID_CANVAS),
            formatMethod("drawPosText", ANDROID_CANVAS),
            formatMethod("drawTextOnPath", ANDROID_CANVAS),
            formatMethod("drawPath", ANDROID_CANVAS),

            formatMethod("setLinearText", ANDROID_PAINT),
            formatMethod("setMaskFilter", ANDROID_PAINT),
            formatMethod("setPathEffect", ANDROID_PAINT),
            formatMethod("setRasterizer", ANDROID_PAINT),
            formatMethod("setSubpixelText", ANDROID_PAINT)
    );

    private static String formatMethod(String method, String aClass) {
        return method + "#" + aClass;
    }

    public UHAQuery(QueryEngine queryEngine) {
        super(KEY, queryEngine);
    }

    /*
        MATCH (m:Method)-[:CALLS]->(e:ExternalMethod)
        WHERE e.full_name parmi UHAs
        RETURN m.app_key

        details -> m.full_name as full_name
        else -> count(m) as UHA
     */

    @Override
    public String getQuery(boolean details) {
        Identifier method = identifier("m");
        Identifier externalMethod = identifier("e");

        return match(node(method).label(METHOD_TYPE)
                .out(CALLS)
                .node(externalMethod).label(EXTERNAL_METHOD_TYPE))
                .where(isUHAMethod(externalMethod))
                .returns(getAlternativeMethodResults(method, details, KEY))
                .toString();
    }

    private BooleanExpression isUHAMethod(Identifier externalMethod) {
        BooleanExpression base = nameMatches(externalMethod, UNSUPPORTED_OPS.get(0));
        for (int i = 1; i < UNSUPPORTED_OPS.size(); i++) {
            base = base.or(nameMatches(externalMethod, UNSUPPORTED_OPS.get(i)));
        }
        return base;
    }

    private BooleanExpression nameMatches(Identifier externalMethod, String call) {
        return externalMethod.property(PaprikaExternalMethod.FULL_NAME).eq(call);
    }

}
