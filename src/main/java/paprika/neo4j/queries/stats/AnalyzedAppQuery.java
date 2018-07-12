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

package paprika.neo4j.queries.stats;

import org.neo4j.cypherdsl.Identifier;
import paprika.entities.PaprikaApp;
import paprika.metrics.app.NumberOfClasses;
import paprika.metrics.app.NumberOfVariables;
import paprika.metrics.classes.condition.IsAbstractClass;
import paprika.metrics.classes.condition.IsInnerClassStatic;
import paprika.metrics.classes.condition.IsInterface;
import paprika.metrics.classes.condition.subclass.*;
import paprika.metrics.common.NumberOfMethods;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

import java.util.Arrays;

import static org.neo4j.cypherdsl.CypherQuery.*;
import static paprika.neo4j.ModelToGraph.APP_TYPE;

public class AnalyzedAppQuery extends PaprikaQuery {

    public static final String COMMAND_KEY = "ANALYZED";

    public AnalyzedAppQuery(QueryEngine engine) {
        super(COMMAND_KEY, engine);
    }

    /*
        MATCH (a:App)
        RETURN  a.app_key as app_key, a.category as category, a.package as package
            a.version_code as version_code, a.date_analysis as date_analysis,a.number_of_classes as number_of_classes,
            a.size as size,a.rating as rating,a.nb_download as nb_download, a.number_of_methods as number_of_methods,
            a.number_of_activities as number_of_activities,a.number_of_services as number_of_services,
            a.number_of_interfaces as number_of_interfaces,a.number_of_abstract_classes as number_of_abstract_classes,
            a.number_of_broadcast_receivers as number_of_broadcast_receivers,a.number_of_content_providers as number_of_content_providers,
            a.number_of_variables as number_of_variables, a.number_of_views as number_of_views,
            a.number_of_inner_classes as number_of_inner_classes, a.number_of_async_tasks as number_of_async_tasks
     */

    @Override
    public String getQuery(boolean details) {
        Identifier app = identifier(("a"));

        return match(node(app).label(APP_TYPE))
                .returns(Arrays.asList(
                        as(app.property(PaprikaApp.APP_KEY), "app_key"),
                        as(app.property(PaprikaApp.CATEGORY), "category"),
                        as(app.property(PaprikaApp.PACKAGE), "package"),
                        as(app.property(PaprikaApp.VERSION_CODE), "version_code"),
                        as(app.property(PaprikaApp.DATE_ANALYSIS), "date_analysis"),
                        as(app.property(NumberOfClasses.NAME), "number_of_classes"),
                        as(app.property(PaprikaApp.SIZE), "size"),
                        as(app.property(PaprikaApp.RATING), "rating"),
                        as(app.property(PaprikaApp.NB_DOWN), "nb_download"),
                        as(app.property(NumberOfMethods.NAME), "number_of_method"),
                        as(app.property(IsActivity.NUMBER_METRIC), "number_of_activities"),
                        as(app.property(IsService.NUMBER_METRIC), "number_of_services"),
                        as(app.property(IsInterface.NUMBER_METRIC), "number_of_interfaces"),
                        as(app.property(IsAbstractClass.NUMBER_METRIC), "number_of_abstract_classes"),
                        as(app.property(IsBroadcastReceiver.NUMBER_METRIC), "number_of_broadcast_receivers"),
                        as(app.property(IsContentProvider.NUMBER_METRIC), "number_of_content_providers"),
                        as(app.property(NumberOfVariables.NAME), "number_of_variables"),
                        as(app.property(IsView.NUMBER_METRIC), "number_of_views"),
                        as(app.property(IsInnerClassStatic.NUMBER_METRIC), "number_of_inner_classes"),
                        as(app.property(IsAsyncTask.NUMBER_METRIC), "number_of_async_tasks")
                )).toString();
    }

}
